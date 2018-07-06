**Overview**

* nuodb-kube-admin-ephemeral-template.yaml
* nuodb-kube-sm-hostpath-template.yaml
* nuodb-kube-te-ephemeral-template.yaml

**Prerequesits**

* Storage HostPath - The NuoDB Storage Manager can optionally run as ephemeral storage but the Storage Manager sample template assumes you have allowed host volume read/write access to containers. To enable pod access to the host edit the SCC Restricted template:

```bash
kubectl patch scc restricted --type=json -p '[{"op": "add", "path": "/allowHostDirVolumePlugin", "value":true}]'
kubectl patch scc restricted --type=json -p '[{"op": "add", "path": "/runAsUser/type", "value":"RunAsAny"}]'
```
* Private Docker Repository - If you are pulling NuoDB from a private Docker repository you will need to create a docker registry secret called "regcred". This secret is used within the YAML templates to authenticate against Docker Hub:
```bash
kubectl create secret docker-registry regcred \
  --docker-server=docker.io \
  --docker-username=<username> \
  --docker-password=<password> \
  --docker-email=<email_address> \
  --namespace=<namespace>
```

**Deploy NuoDB on Kubernetes**

**NuoAdmin**

```bash
kubectl create -f https://raw.githubusercontent.com/nuodb/nuodb-samples/feature/kubernetes-template/Kubernetes/nuodb-kube-admin-ephemeral-template.yaml
```

Parameter | Description | Default
--------- | ----------- | -------
`namespace` | Which kubernetes namespace to deploy resource to | default


**Storage Manager**

**Transaction Engine**




