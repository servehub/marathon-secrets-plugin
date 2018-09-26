# Marathon Secret Vars

Marathon plugin for receive secrets form some URL, decode and pass as ENV variables

## Install to marathon

### Create file with secrets:
```
{
  "secrets": {
    "some.test.value": {
      "value": "LS0tLS1CRUdJT...==",
      "target": [
        { "app": "/group/app-v1.2.4" }
      ]
    },
    "other.secret.pass": {
      "value": "LS0tLR3FS5TJT...==",
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
    "secretVars": {
      "plugin": "mesosphere.marathon.plugin.task.RunSpecTaskProcessor",
      "implementation": "servehub.marathon.plugin.MarathonSecretsPlugin",
      "configuration": {
        "varPrefix": "SECRET_",
        "privateKey": "LS0tLS1C...",
        "secretsUrl": "http://127.0.0.1:8500/v1/kv/services/secrets?raw=true"
      }
    }
  }
}
```

Where `privateKey` is `cat private.key | base64`.

### Register plugin jar and config file in marathon:
Download plugin from https://github.com/servehub/marathon-secrets-plugin/releases/download/v1.0.0/marathon-secrets-plugin-assembly-1.0.0.jar

```
scp plugin-conf.json root@server:/etc/marathon/
scp marathon-secrets-plugin-assembly-1.0.0.jar root@server:/etc/marathon/plugins/
```

Restart marathon with arguments:
```
--plugin_dir=/etc/marathon/plugins --plugin_conf=/etc/marathon/plugin-conf.json
```

## Development

Build plugin jar

```
sbt clean test assembly
```
