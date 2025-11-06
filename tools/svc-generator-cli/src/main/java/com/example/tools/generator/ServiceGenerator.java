package com.example.tools.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ServiceGenerator {
    public static void main(String[] args) throws Exception {
        Map<String, String> cli = parseArgs(args);
        String specPath = required(cli, "--spec");
        String outDir = required(cli, "--out");
        String groupId = cli.getOrDefault("--groupId", "com.example");
        String artifactId = cli.getOrDefault("--artifactId", "generated-service");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode spec = mapper.readTree(new File(specPath));

        String boundedContext = opt(spec.at("/x-ddd/boundedContext"), "App");
        String basePackage = cli.getOrDefault("--package", (groupId + "." + toPackage(boundedContext)).toLowerCase());

        // New: SQL generation mode
        boolean sqlOnly = cli.containsKey("--sql") || cli.containsKey("--generate-sql");
        Path outRoot = Paths.get(outDir).toAbsolutePath();
        Files.createDirectories(outRoot);
        if (sqlOnly) {
            String sql = generateSql(spec);
            Path sqlFile = outRoot.resolve("schema.sql");
            writeString(sqlFile, sql);
            System.out.println("[Generator] SQL schema generated at: " + sqlFile);
            return;
        }

        Path projectRoot = outRoot;
        System.out.println("[Generator] Output directory: " + projectRoot);
        Files.createDirectories(projectRoot);

        // Generate Maven project files
        writeString(projectRoot.resolve("pom.xml"), pomXml(groupId, artifactId, boundedContext));

        // src structure
        Path javaRoot = projectRoot.resolve(Paths.get("src", "main", "java"));
        Path resourcesRoot = projectRoot.resolve(Paths.get("src", "main", "resources"));
        Files.createDirectories(javaRoot);
        Files.createDirectories(resourcesRoot);

        Path packageRoot = javaRoot.resolve(basePackage.replace('.', File.separatorChar));
        Files.createDirectories(packageRoot);

        // Application class
        writeString(packageRoot.resolve(boundedContext + "Application.java"), applicationJava(basePackage, boundedContext));

        // Parse DTO schemas
        List<DTOSchema> dtoSchemas = extractDtos(spec);
        Path dtoDir = packageRoot.resolve("dto");
        Files.createDirectories(dtoDir);
        for (DTOSchema dto : dtoSchemas) {
            writeString(dtoDir.resolve(dto.name + ".java"), dtoJava(basePackage + ".dto", dto));
        }

        // Determine controller + service from paths
        List<ApiOperation> operations = extractOperations(spec);
        if (!operations.isEmpty()) {
            // For simplicity, one controller and one service per spec
            String serviceName = operations.get(0).serviceName;
            Path apiDir = packageRoot.resolve("api");
            Files.createDirectories(apiDir);
            writeString(apiDir.resolve("" + "ApiController.java"), controllerJava(basePackage, operations));

            Path svcDir = packageRoot.resolve("service");
            Files.createDirectories(svcDir);
            writeString(svcDir.resolve(serviceName + ".java"), serviceJava(basePackage, serviceName, operations));
        }

        // application.yml
        writeString(resourcesRoot.resolve("application.yml"), "server:\n  port: 8080\n");

        // README
        writeString(projectRoot.resolve("README.md"), readmeMd(boundedContext));

        System.out.println("[Generator] Done. Project generated at: " + projectRoot);
    }

    // DTO extraction
    private static List<DTOSchema> extractDtos(JsonNode spec) {
        List<DTOSchema> result = new ArrayList<>();
        JsonNode schemas = spec.at("/components/schemas");
        if (schemas != null && schemas.isObject()) {
            Iterator<String> names = schemas.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                JsonNode schema = schemas.get(name);
                // DTO criteria: object type AND not persistence entity AND not value object
                boolean isObject = "object".equals(opt(schema.get("type"), ""));
                boolean hasPersistence = schema.has("x-persistence");
                boolean isValueObject = schema.has("x-ddd") && schema.get("x-ddd").has("isValueObject") && schema.get("x-ddd").get("isValueObject").asBoolean(false);
                if (isObject && !hasPersistence && !isValueObject) {
                    DTOSchema dto = new DTOSchema();
                    dto.name = name;
                    dto.fields = new ArrayList<>();
                    JsonNode props = schema.get("properties");
                    Set<String> required = new HashSet<>();
                    if (schema.has("required")) {
                        for (JsonNode r : schema.get("required")) required.add(r.asText());
                    }
                    if (props != null && props.isObject()) {
                        Iterator<String> fns = props.fieldNames();
                        while (fns.hasNext()) {
                            String fn = fns.next();
                            JsonNode prop = props.get(fn);
                            dto.fields.add(extractField(fn, prop, required.contains(fn)));
                        }
                    }
                    result.add(dto);
                }
            }
        }
        return result;
    }

    private static DTOField extractField(String name, JsonNode prop, boolean required) {
        DTOField f = new DTOField();
        f.name = name;
        String type = opt(prop.get("type"), "string");
        String format = opt(prop.get("format"), null);
        f.javaType = mapJavaType(type, format);
        f.required = required;
        if (prop.has("minLength")) f.minLength = prop.get("minLength").asInt();
        if (prop.has("maxLength")) f.maxLength = prop.get("maxLength").asInt();
        if ("email".equals(format)) f.isEmail = true;
        return f;
    }

    // Paths extraction
    private static List<ApiOperation> extractOperations(JsonNode spec) {
        List<ApiOperation> ops = new ArrayList<>();
        JsonNode paths = spec.get("paths");
        if (paths != null && paths.isObject()) {
            Iterator<String> pnames = paths.fieldNames();
            while (pnames.hasNext()) {
                String path = pnames.next();
                JsonNode item = paths.get(path);
                Iterator<String> methods = item.fieldNames();
                while (methods.hasNext()) {
                    String method = methods.next();
                    JsonNode op = item.get(method);
                    String svcOp = opt(op.get("x-service-operation"), null);
                    if (svcOp == null) continue;
                    String[] parts = svcOp.split("\\.");
                    String serviceName = parts.length > 0 ? parts[0] : "AppService";
                    String operationName = parts.length > 1 ? parts[1] : opt(op.get("operationId"), "op");

                    String reqDto = null;
                    JsonNode rb = op.get("requestBody");
                    if (rb != null) {
                        JsonNode schema = rb.at("/content/application~1json/schema");
                        reqDto = refName(schema);
                    }

                    String resDto = null;
                    JsonNode res201 = op.at("/responses/201/content/application~1json/schema");
                    if (!res201.isMissingNode()) resDto = refName(res201);

                    ops.add(new ApiOperation(path, method.toUpperCase(Locale.ROOT), serviceName, operationName, reqDto, resDto));
                }
            }
        }
        return ops;
    }

    // Writers
    private static String pomXml(String groupId, String artifactId, String boundedContext) {
        return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>" + esc(groupId) + "</groupId>\n" +
                "    <artifactId>" + esc(artifactId) + "</artifactId>\n" +
                "    <version>1.0.0</version>\n" +
                "    <name>" + esc(boundedContext) + " Service</name>\n" +
                "    <properties>\n" +
                "        <java.version>17</java.version>\n" +
                "        <spring-boot.version>3.3.3</spring-boot.version>\n" +
                "    </properties>\n" +
                "    <dependencyManagement>\n" +
                "        <dependencies>\n" +
                "            <dependency>\n" +
                "                <groupId>org.springframework.boot</groupId>\n" +
                "                <artifactId>spring-boot-dependencies</artifactId>\n" +
                "                <version>${spring-boot.version}</version>\n" +
                "                <type>pom</type>\n" +
                "                <scope>import</scope>\n" +
                "            </dependency>\n" +
                "        </dependencies>\n" +
                "    </dependencyManagement>\n" +
                "    <dependencies>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework.boot</groupId>\n" +
                "            <artifactId>spring-boot-starter-web</artifactId>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework.boot</groupId>\n" +
                "            <artifactId>spring-boot-starter-validation</artifactId>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.projectlombok</groupId>\n" +
                "            <artifactId>lombok</artifactId>\n" +
                "            <optional>true</optional>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.springframework.boot</groupId>\n" +
                "                <artifactId>spring-boot-maven-plugin</artifactId>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>\n";
    }

    private static String applicationJava(String basePackage, String boundedContext) {
        return "package " + basePackage + ";\n\n" +
                "import org.springframework.boot.SpringApplication;\n" +
                "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" +
                "@SpringBootApplication\n" +
                "public class " + boundedContext + "Application {\n" +
                "    public static void main(String[] args) {\n" +
                "        SpringApplication.run(" + boundedContext + "Application.class, args);\n" +
                "    }\n" +
                "}\n";
    }

    private static String dtoJava(String pkg, DTOSchema dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import jakarta.validation.constraints.*;\n");
        sb.append("import lombok.Data;\n\n");
        sb.append("@Data\n");
        sb.append("public class ").append(dto.name).append(" {\n");
        for (DTOField f : dto.fields) {
            List<String> anns = new ArrayList<>();
            if (f.required) anns.add("@NotNull");
            if ("String".equals(f.javaType)) {
                if (f.required) anns.add("@NotBlank");
                if (f.minLength != null || f.maxLength != null) {
                    StringBuilder size = new StringBuilder("@Size(");
                    if (f.minLength != null) size.append("min=").append(f.minLength);
                    if (f.minLength != null && f.maxLength != null) size.append(", ");
                    if (f.maxLength != null) size.append("max=").append(f.maxLength);
                    size.append(")");
                    anns.add(size.toString());
                }
                if (f.isEmail) anns.add("@Email");
            }
            for (String a : anns) sb.append("    ").append(a).append("\n");
            sb.append("    private ").append(f.javaType).append(" ").append(f.name).append(";\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String controllerJava(String basePackage, List<ApiOperation> ops) {
        String pkg = basePackage + ".api";
        String svcPkg = basePackage + ".service";
        String dtoPkg = basePackage + ".dto";
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.springframework.http.ResponseEntity;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import ").append(svcPkg).append(".*;\n");
        sb.append("import ").append(dtoPkg).append(".*;\n\n");
        sb.append("@RestController\n");
        sb.append("public class ApiController {\n\n");
        String serviceName = ops.get(0).serviceName;
        sb.append("    private final ").append(serviceName).append(" service;\n\n");
        sb.append("    @Autowired\n");
        sb.append("    public ApiController(").append(serviceName).append(" service) {\n");
        sb.append("        this.service = service;\n");
        sb.append("    }\n\n");
        for (ApiOperation op : ops) {
            String mappingAnn = switch (op.method) {
                case "GET" -> "@GetMapping";
                case "POST" -> "@PostMapping";
                case "PUT" -> "@PutMapping";
                case "DELETE" -> "@DeleteMapping";
                default -> "@RequestMapping";
            };
            sb.append("    ").append(mappingAnn).append("(\"").append(op.path).append("\")\n");
            String reqType = op.requestDto != null ? op.requestDto : "Void";
            String resType = op.responseDto != null ? op.responseDto : "Void";
            sb.append("    public ResponseEntity<").append(resType).append("> ")
                    .append(op.operationName)
                    .append("(@RequestBody(required=")
                    .append(!"Void".equals(reqType))
                    .append(") ")
                    .append(" ")
                    .append("payload")
                    .append(!"Void".equals(reqType) ? ": " + reqType : ": Void")
                    .append(") {\n");
            sb.append("        ");
            if (!"Void".equals(resType)) {
                sb.append(resType).append(" result = service.").append(op.operationName)
                        .append(!"Void".equals(reqType) ? "(payload);" : "();").append("\n");
                sb.append("        return ResponseEntity.status(201).body(result);\n");
            } else {
                sb.append("service.").append(op.operationName)
                        .append(!"Void".equals(reqType) ? "(payload);" : "();").append("\n");
                sb.append("        return ResponseEntity.accepted().build();\n");
            }
            sb.append("    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String serviceJava(String basePackage, String serviceName, List<ApiOperation> ops) {
        String pkg = basePackage + ".service";
        String dtoPkg = basePackage + ".dto";
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.springframework.stereotype.Service;\n");
        sb.append("import ").append(dtoPkg).append(".*;\n\n");
        sb.append("@Service\n");
        sb.append("public class ").append(serviceName).append(" {\n\n");
        for (ApiOperation op : ops) {
            String reqType = op.requestDto != null ? op.requestDto : null;
            String resType = op.responseDto != null ? op.responseDto : "void";
            sb.append("    public ").append(resType.equals("void") ? "void" : resType)
                    .append(" ").append(op.operationName).append("(");
            if (reqType != null) {
                sb.append(reqType).append(" payload");
            }
            sb.append(") {\n");
            sb.append("        // TODO: Implement business logic as per x-business-logic\n");
            if (!resType.equals("void")) {
                sb.append("        throw new UnsupportedOperationException(\"Not implemented yet\");\n");
            }
            sb.append("    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String readmeMd(String boundedContext) {
        return "# " + boundedContext + " Generated Service\n\n" +
                "This project was generated from an app-spec.ddd.json using the svc-generator-cli.\n\n" +
                "## Build & Run\n\n" +
                "- mvn spring-boot:run\n";
    }

    // Helpers
    private static String refName(JsonNode schemaNode) {
        if (schemaNode == null || schemaNode.isMissingNode()) return null;
        if (schemaNode.has("$ref")) {
            String ref = schemaNode.get("$ref").asText();
            int idx = ref.lastIndexOf('/');
            return idx >= 0 ? ref.substring(idx + 1) : ref;
        }
        return null;
    }

    private static String esc(String s) { return s == null ? "" : s; }

    private static String toPackage(String s) {
        return s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    private static String opt(JsonNode node, String def) {
        return node == null || node.isMissingNode() || node.isNull() ? def : node.asText();
    }

    private static void writeString(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        try (FileWriter fw = new FileWriter(file.toFile())) {
            fw.write(content);
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                String val = (i + 1 < args.length && !args[i+1].startsWith("--")) ? args[++i] : "true";
                map.put(a, val);
            }
        }
        return map;
    }

    private static String mapJavaType(String type, String format) {
        if ("integer".equals(type)) return "Integer";
        if ("number".equals(type)) return "Double";
        if ("boolean".equals(type)) return "Boolean";
        if ("string".equals(type) && "uuid".equals(format)) return "java.util.UUID";
        if ("string".equals(type)) return "String";
        return "String";
    }

    // ===== SQL GENERATION =====
    private static String generateSql(JsonNode spec) {
        Map<String, EntityDef> entities = extractEntities(spec);
        // Resolve FK target info now that we know PKs
        for (EntityDef e : entities.values()) {
            for (ForeignKey fk : e.foreignKeys) {
                EntityDef target = entities.get(fk.targetEntityName);
                if (target != null) {
                    fk.targetTable = target.tableName;
                    fk.targetColumn = target.primaryKeyColumn;
                    // set data type of FK column to match target PK if not already set
                    Column c = e.findColumn(fk.columnName);
                    if (c != null && (c.dataType == null || c.dataType.isEmpty())) {
                        c.dataType = target.primaryKeyType;
                    }
                }
            }
        }
        List<EntityDef> ordered = topologicalOrder(new ArrayList<>(entities.values()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            EntityDef e = ordered.get(i);
            sb.append(createTableSql(e));
            if (i < ordered.size() - 1) sb.append("\n\n");
        }
        return sb.toString();
    }

    private static Map<String, EntityDef> extractEntities(JsonNode spec) {
        Map<String, EntityDef> map = new LinkedHashMap<>();
        JsonNode schemas = spec.at("/components/schemas");
        if (schemas != null && schemas.isObject()) {
            Iterator<String> names = schemas.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                JsonNode schema = schemas.get(name);
                JsonNode xp = schema.get("x-persistence");
                boolean isEntity = xp != null && xp.has("isEntity") && xp.get("isEntity").asBoolean(false);
                if (!isEntity) continue;
                String tableName = xp.has("tableName") ? xp.get("tableName").asText() : toSnake(name);
                EntityDef e = new EntityDef();
                e.schemaName = name;
                e.tableName = tableName;
                e.columns = new ArrayList<>();
                e.foreignKeys = new ArrayList<>();

                JsonNode props = schema.get("properties");
                if (props != null && props.isObject()) {
                    Iterator<String> fns = props.fieldNames();
                    while (fns.hasNext()) {
                        String fn = fns.next();
                        JsonNode prop = props.get(fn);
                        JsonNode fp = prop.get("x-persistence");
                        // check relation
                        if (fp != null && fp.has("relation")) {
                            JsonNode rel = fp.get("relation");
                            String joinColumn = rel.has("joinColumn") ? rel.get("joinColumn").asText(null) : null;
                            String targetEntity = rel.has("targetEntity") ? rel.get("targetEntity").asText() : null;
                            if (joinColumn != null && targetEntity != null) {
                                // owning side, create FK column (type resolved later) and FK record
                                Column col = new Column();
                                col.name = joinColumn;
                                col.dataType = null; // resolve later
                                col.nullable = true; // default allow null unless specified otherwise
                                e.columns.add(col);
                                ForeignKey fk = new ForeignKey();
                                fk.columnName = joinColumn;
                                fk.targetEntityName = targetEntity;
                                e.foreignKeys.add(fk);
                            }
                            // if mappedBy present, skip (inverse side)
                            continue;
                        }
                        // normal column
                        Column col = new Column();
                        col.name = fp != null && fp.has("columnName") ? fp.get("columnName").asText() : toSnake(fn);
                        String type = prop.has("type") ? prop.get("type").asText() : null;
                        String format = prop.has("format") ? prop.get("format").asText() : null;
                        col.dataType = fp != null && fp.has("dataType") ? fp.get("dataType").asText() : mapSqlType(type, format);
                        col.nullable = !(fp != null && fp.has("isNullable") && !fp.get("isNullable").asBoolean(false) ? false : true) ? false : (fp != null && fp.has("isNullable") ? fp.get("isNullable").asBoolean(false) : false);
                        // The above line is too complex; simplify
                        col.nullable = fp != null && fp.has("isNullable") ? fp.get("isNullable").asBoolean(false) : false;
                        col.unique = fp != null && fp.has("isUnique") && fp.get("isUnique").asBoolean(false);
                        if (fp != null && fp.has("isPrimaryKey") && fp.get("isPrimaryKey").asBoolean(false)) {
                            e.primaryKeyColumn = col.name;
                            e.primaryKeyType = col.dataType;
                        }
                        e.columns.add(col);
                    }
                }
                map.put(name, e);
            }
        }
        return map;
    }

    private static String mapSqlType(String type, String format) {
        if ("string".equals(type) && "uuid".equals(format)) return "uuid";
        if ("string".equals(type) && "date-time".equals(format)) return "timestamp_with_timezone";
        if ("integer".equals(type)) return "integer";
        if ("number".equals(type)) return "numeric";
        if ("boolean".equals(type)) return "boolean";
        if ("string".equals(type)) return "varchar(255)";
        return "text";
    }

    private static List<EntityDef> topologicalOrder(List<EntityDef> entities) {
        Map<String, EntityDef> byTable = new HashMap<>();
        for (EntityDef e : entities) byTable.put(e.tableName, e);
        Map<String, Set<String>> deps = new HashMap<>();
        for (EntityDef e : entities) {
            Set<String> set = new HashSet<>();
            for (ForeignKey fk : e.foreignKeys) {
                if (fk.targetTable != null) set.add(fk.targetTable);
            }
            deps.put(e.tableName, set);
        }
        List<EntityDef> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (EntityDef e : entities) dfs(e.tableName, deps, visited, new HashSet<>(), byTable, ordered);
        return ordered;
    }

    private static void dfs(String node, Map<String, Set<String>> deps, Set<String> visited, Set<String> stack, Map<String, EntityDef> byTable, List<EntityDef> out) {
        if (visited.contains(node)) return;
        if (stack.contains(node)) { // cycle
            visited.add(node);
            out.add(byTable.get(node));
            return;
        }
        stack.add(node);
        for (String d : deps.getOrDefault(node, Collections.emptySet())) dfs(d, deps, visited, stack, byTable, out);
        stack.remove(node);
        visited.add(node);
        out.add(byTable.get(node));
    }

    private static String createTableSql(EntityDef e) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(e.tableName).append(" (\n");
        List<String> lines = new ArrayList<>();
        for (Column c : e.columns) {
            StringBuilder cl = new StringBuilder();
            cl.append("    ").append(c.name).append(" ").append(c.dataType != null ? c.dataType : "text");
            if (!c.nullable) cl.append(" NOT NULL");
            if (c.unique) cl.append(" UNIQUE");
            lines.add(cl.toString());
        }
        if (e.primaryKeyColumn != null) {
            lines.add("    PRIMARY KEY (" + e.primaryKeyColumn + ")");
        }
        for (ForeignKey fk : e.foreignKeys) {
            if (fk.targetTable != null && fk.targetColumn != null) {
                lines.add("    FOREIGN KEY (" + fk.columnName + ") REFERENCES " + fk.targetTable + "(" + fk.targetColumn + ")");
            }
        }
        sb.append(String.join(",\n", lines));
        sb.append("\n);");
        return sb.toString();
    }

    private static String toSnake(String s) {
        return s.replaceAll("([a-z])([A-Z])", "$1_$2").replaceAll("[^A-Za-z0-9_]", "_").toLowerCase();
    }

    // Models
    static class DTOSchema {
        String name;
        List<DTOField> fields;
    }
    static class DTOField {
        String name;
        String javaType;
        boolean required;
        Integer minLength;
        Integer maxLength;
        boolean isEmail;
    }

    static class ApiOperation {
        String path;
        String method;
        String serviceName;
        String operationName;
        String requestDto;
        String responseDto;
        ApiOperation(String path, String method, String serviceName, String operationName, String requestDto, String responseDto) {
            this.path = path; this.method = method; this.serviceName = serviceName; this.operationName = operationName; this.requestDto = requestDto; this.responseDto = responseDto;
        }
    }

    static class EntityDef {
        String schemaName;
        String tableName;
        List<Column> columns;
        String primaryKeyColumn;
        String primaryKeyType;
        List<ForeignKey> foreignKeys;
        Column findColumn(String name) {
            for (Column c : columns) if (c.name.equals(name)) return c;
            return null;
        }
    }
    static class Column {
        String name;
        String dataType;
        boolean nullable;
        boolean unique;
    }
    static class ForeignKey {
        String columnName;
        String targetEntityName;
        String targetTable;
        String targetColumn;
    }

    private static String required(Map<String, String> map, String key) {
        String v = map.get(key);
        if (v == null || v.isBlank()) {
            System.err.println("Missing required argument: " + key);
            System.err.println("Usage: java -jar svc-generator-cli.jar --spec <path> --out <dir> [--groupId g] [--artifactId a] [--package p]");
            System.exit(1);
        }
        return v;
    }
}
