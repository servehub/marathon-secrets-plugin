# Marathon Secret Vars

Marathon plugin for receive secrets form some URL, decode and pass as ENV variables

## Install to marathon

### Generate config file:
```
{
  "secrets": {
    "some.test.value": {
      "value": {
        "dev": "ENC:LS0tLS1CRUdJT...==",
        "prod": "ENC:LS0tLS1CRUdJTiB...=="
      },
      "target": [
        { "app": "/group/app-v1.2.4" }
      ]
    },
    "open.secret.pass": {
      "value": {
        "dev": "open-value-dev",
        "prod": "open-value-prod"
      },
      "target": [
        { "app": "/group/" }
      ]
    }
  }
}
```

For encrypt value you should use openssl:

1. Create priv/pub keys
```
  openssl req -x509 -nodes -newkey rsa:4096 -keyout private.key -out public.cer -subj "/CN=PKCS#7"
```

2. Encrypt value:
```
echo -n "my-pass-123" | openssl smime -encrypt -outform pem public.cer | base64
```
  
### Save file with secrets to any http server

For example save it to consul kv:
```
curl -XPUT http://127.0.0.1:8500/v1/kv/services/secrets -d @secrets.json
```

### Create config file for marathon:
```
{
  "plugins": {
    "secureVars": {
      "plugin": "mesosphere.marathon.plugin.task.RunSpecTaskProcessor",
      "implementation": "servehub.marathon.secures.plugin.SecureVarsPlugin",
      "configuration": {
        "env": "dev",
        "varPrefix": "SECURE_",
        "privateKey": "LS0tLS1C...",
        "secretsUrl": "http://127.0.0.1:8500/v1/kv/services/secrets?raw=true"
      }
    }
  }
}
```

Where `privateKey` is `cat private.key | base64`.

### Register plugin jar and config file in marathon:
```
scp plugin-conf.json root@server:/etc/marathon/
scp marathon-secures-assembly-1.0.jar root@server:/etc/marathon/plugins/
```

Restart marathon with arguments:
```
--plugin_dir=/etc/marathon/plugins --plugin_conf=/etc/marathon/plugin-conf.json
```

## Build plugin jar

```
sbt clean test assembly
```
