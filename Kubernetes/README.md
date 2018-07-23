**Overview**

The following instructions will guide you through deploying NuoDB on Kubernetes with the following templates.

* nuodb-kube-admin-ephemeral-template.yaml
* nuodb-kube-sm-hostpath-template.yaml
* nuodb-kube-te-ephemeral-template.yaml

**Prerequesits**

* Storage HostPath - The NuoDB Storage Manager can optionally run as ephemeral storage but the Storage Manager sample template assumes you have allowed host volume read/write access to containers. To enable pod access to the host edit the SCC Restricted template:

```bash
kubectl patch scc restricted --type=json -p '[{"op": "add", "path": "/allowHostDirVolumePlugin", "value":true}]'
kubectl patch scc restricted --type=json -p '[{"op": "add", "path": "/runAsUser/type", "value":"RunAsAny"}]'
```

* Persistent Storage - Because the Storage Manager (SM) pod is using HostPath to write directly to the host volume you will need to force the SM to only spawn on that particular host. To achieve this, you will need to label your storage node and use the nodeSelector in the yaml template to deploy the SM pod to the node. To label the node use the following:

```bash
kubectl label node <node_name> smStorage=sm0
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

The NuoDB deployment has been broken into 3 yaml files to allow for more flexibility. As long as you have at least one cluster of NuoAdmins deployed, you can added as many SM or TEs to the existing NuoDB domain as needed.

**NuoAdmin**

You will need to deploy the NuoDB domain before deploying the storage manager and transaction engines. Download the yaml template to where you can run kubectl and manage your Kubernetes cluster.
```bash
curl -sl https://raw.githubusercontent.com/nuodb/nuodb-samples/feature/kubernetes-template/Kubernetes/nuodb-kube-admin-ephemeral-template.yaml -o ./sm.yaml
```

This template does not have any dynamic parameters and is not wrapped with Helm Charts so you will need to manually edit the parameters to match your environment directly within the yaml template. The following chart lists the parameters that need to be updated:


Key | Parameter | Description 
--------- | ----------- | -------
`namespace` | <namespace_name> | Designate which kubernetes namespace for resource 
`service name` | <admin_service_name> | PEER_ADDRESS and service name for Admin load balancing and application connection connection string 
`statefulset name` | nuoadmin-<node_zone_name> | The statefulset deployment group's name. It's best to associate the name with the zone you are deploying NuoDB to. Append the node_zone_name to 'nouadmin-' 
`container name` | nuoadmin-<node_zone_name> | Name used for deploying pods. It's best to associate the name with deployment zone. Append the node_zone_name to 'nuoadmin-'
`container env node region` | <node_zone_name> | Node zone location for regional deployments of NuoDB 


Now you are ready to deploy the admin layer of NuoDB

```bash
kubectl create -f ./admin.yaml
```

Check the status of the deployment with the following command:

```bash
kubectl describe statefulset <name_of_admin_statefulset>
```

You should see the following status:
```text
Replicas:		3 desired | 3 total
Pods Status:		3 Running / 0 Waiting / 0 Succeeded / 0 Failed
```

Check on the status of the NuoDB domain with the following command:

```bash
kubectl exec <nuoadmin_pod_name> show_domain
```
Results:
```text
server version: 3.3.master-5609-f930f4b139, server license: Enterprise
server time: 2018-07-20T17:26:31.246000
Servers:
  [nuoadmin-us-east-2a-0] nuoadmin-us-east-2a-0.nuoadmin-dns:48005 (LEADER, Leader=nuoadmin-us-east-2a-0) ACTIVE:Connected
  [nuoadmin-us-east-2a-1] nuoadmin-us-east-2a-1.nuoadmin-dns:48005 (FOLLOWER, Leader=nuoadmin-us-east-2a-0) ACTIVE:Connected
  [nuoadmin-us-east-2a-2] nuoadmin-us-east-2a-2.nuoadmin-dns:48005 (FOLLOWER, Leader=nuoadmin-us-east-2a-0) ACTIVE:Connected *
```

You are now ready to deploy the SM and TE containers

**Storage Manager**

 Download the yaml template to where you can run kubectl and manage your Kubernetes cluster.
```bash
curl -sl https://raw.githubusercontent.com/nuodb/nuodb-samples/feature/kubernetes-template/Kubernetes/nuodb-kube-admin-ephemeral-template.yaml -o ./admin.yaml
```

In this template, a kubernetes secret is created to store the username and password for the database. The username and password must be converted to base64 in order to be processed by kubernetes. You can do this from a terminal with the following command:

```bash
echo '<user_name>' | base64
echo '<user_pass>' | base64
```

Manually edit the parameters to match your environment directly within the yaml template. The following chart lists the parameters that need to be updated:

Key | Parameter | Description 
--------- | ----------- | -------
`namespace` | <namespace_name> | Designate which kubernetes namespace for resource. Should match namespace used for NuoAdmin 
`container env node region` | <node_zone_name> | Node zone location for regional deployments of NuoDB 
`peer_address` | <admin_service_name> | Service name for Admin load balancing. SM will use service to register on domain 
`replicaset name` | sm0-<node_zone_name> | The replicaset deployment group's name. It's best to associate the name with the zone you are deploying NuoDB to. Append the node_zone_name to 'sm0-' 
`container name` | sm0-<node_zone_name> | Name used for deploying pods. It's best to associate the name with deployment zone. Append the node_zone_name to 'sm0-'
`DB_NAME` | <DB_NAME> | Database name that will be created on start of the SM
`DB_USER` | <DB_USER> | Base64 value for database user
`DB_PASS` | <DB_PASS> | Base64 value for database user password
`archive hostpath` | <archive_storage_path> | Host directory path. example: /local-storage/archive
`journal hostpath` | <journal_storage_path> | Host directory path. example: /local-storage/journal


Deploy the NuoDB Storage Manager

```bash
kubectl create -f ./sm.yaml
```


**Transaction Engine**

 Download the yaml template to where you can run kubectl and manage your Kubernetes cluster.
```bash
curl -sl https://raw.githubusercontent.com/nuodb/nuodb-samples/feature/kubernetes-template/Kubernetes/nuodb-kube-admin-ephemeral-template.yaml -o ./te.yaml
```

Manually edit the parameters to match your environment directly within the yaml template. The following chart lists the parameters that need to be updated:

Key | Parameter | Description 
--------- | ----------- | -------
`namespace` | <namespace_name> | Designate which kubernetes namespace for resource. Should match namespace used for NuoAdmin 
`container env node region` | <node_zone_name> | Node zone location for regional deployments of NuoDB 
`peer_address` | <admin_service_name> | Service name for Admin load balancing. SM will use service to register on domain 
`replicaset name` | te-<node_zone_name> | The replicaset deployment group's name. It's best to associate the name with the zone you are deploying NuoDB to. Append the node_zone_name to 'te-' 
`container name` | te-<node_zone_name> | Name used for deploying pods. It's best to associate the name with deployment zone. Append the node_zone_name to 'te-'



Deploy the NuoDB Transaction Engine

```bash
kubectl create -f ./te.yaml
```




