# service-schema

A small repository of JSON Schemas that describe service/application specifications and business specifications. These schemas can be used to validate spec files in CI, drive code generation, or provide structured context for LLMs.

## Contents

- app-spec.v1.json — Application/service specification schema (versioned: v1)
- app-spec.ddd.json — Application/service specification schema with DDD-oriented structure (experimental)
- business-spec.schema.v1.json — Business-domain specification schema (versioned: v1)
- business-spec.schema.ddd.json — Business-domain specification schema with DDD-oriented structure (experimental)
- llm_context_schema.md — Notes/guidance for using these schemas as context for LLMs

Naming conventions:
- *.vN.json files are versioned and intended to be stable at that version.
- *.ddd.json files are experimental variants that lean into Domain-Driven Design (DDD) concepts and may change.

## Getting started

You can use any JSON Schema validator. Below are quick examples with popular tools.

### Validate with Node.js (ajv)

1) Install CLI:

```
npm install -g ajv-cli
```

2) Validate a spec file (example uses app-spec.v1.json):

```
ajv validate -s app-spec.v1.json -d path/to/your/app-spec.json --strict=false
```

- Use `--strict=false` if your validator is strict about unknown keywords.

### Validate with Python (jsonschema)

```
pip install jsonschema
python - <<PY
import json, sys
from jsonschema import validate, Draft202012Validator

schema = json.load(open('app-spec.v1.json'))
instance = json.load(open('path/to/your/app-spec.json'))
Draft202012Validator.check_schema(schema)
validate(instance=instance, schema=schema)
print('Valid!')
PY
```

## Versioning policy

- v1 schemas aim to remain backward compatible for bug fixes; breaking changes will result in v2, v3, ... files.
- ddd schemas are experimental and may change without backward compatibility guarantees.

## Contributing

- Open an issue or PR if you find inconsistencies or have suggestions.
- When proposing changes, please:
  - Keep the versioned schemas backward compatible where possible.
  - Add minimal examples and validate them against the updated schema.

## License

This project is licensed under the Apache License, Version 2.0.
See the LICENSE file for details.

---
Last updated: 2025-11-04 20:35 (local)