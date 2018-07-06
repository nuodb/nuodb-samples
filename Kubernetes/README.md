###Overview

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

##Deploy NuoDB on Kubernetes

###NuoAdmin

Parameter | Description | Default
--------- | ----------- | -------
`cluster.join` | The cluster join string.  See [cluster discovery](https://docs.storageos.com/docs/install/prerequisites/clusterdiscovery) documentation for details.
`image.repository` | StorageOS container image repository | `storageos/node`
`image.tag` | StorageOS container image tag | `1.0.0-rc2`
`image.pullPolicy` | StorageOS container image pull policy | `IfNotPresent`
`initContainer.repository` | StorageOS init container image repository | `storageos/init`
`initContainer.tag` | StorageOS init container image tag | `0.1`
`initContainer.pullPolicy` | StorageOS init container image pull policy | `IfNotPresent`
`rbacEnabled` | Use of k8s RBAC features | `true`
`storageclass.name` | StorageOS storage class name | `fast`
`storageclass.pool` | Default storage pool for storage class | `default`
`storageclass.fsType` | Default filesystem type for storage class | `ext4`
`api.secretName` | Name of the secret used for storing api location and credentials | `storageos-api`
`api.secretNamespace` | Namespace of the secret used for storing api location and credentials. Needed in every namespace to use StorageOS. | `default`
`api.address` | Hostname or IP address of the external StorageOS api endpoint.  This must be accessible from the Kubernetes master. | `http://storageosapi:5705`
`api.username` | Username to authenticate to the StorageOS api with | `storageos`
`api.password` | Password to authenticate to the StorageOS api with | `storageos`
`service.name` | Name of the StorageOS service | `storageos`
`service.externalPort` | External service port | `5705`
`service.internalPort` | Internal service port | `5705`
`resources` | Pod resource requests & limits | `{}`
###Storage Manager

###Transaction Engine




