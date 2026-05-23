# External RAG Sample: Kubernetes Persistent Volumes

Source URL: https://kubernetes.io/docs/concepts/storage/persistent-volumes/
Prepared for: long technical docs, lifecycle terms, recovery procedures, unrelated technical refusal.
Snapshot note: compact offline sample prepared on 2026-05-23 from Kubernetes persistent volume documentation.

## Introduction

Kubernetes separates storage from the lifecycle of individual Pods by using PersistentVolume and PersistentVolumeClaim objects.

A PersistentVolume, or PV, is a piece of storage in the cluster. It may be provisioned by an administrator or dynamically provisioned through a StorageClass. A PV is a cluster resource with a lifecycle independent of any individual Pod that uses it.

A PersistentVolumeClaim, or PVC, is a user's request for storage. PVCs consume PV resources in a similar way that Pods consume node resources. A PVC can request storage size and access modes such as ReadWriteOnce, ReadOnlyMany, ReadWriteMany, or ReadWriteOncePod.

## Lifecycle

The high-level lifecycle includes provisioning, binding, using, storage object protection, reclaiming, expansion, and deletion protection. A typical retrieval task should distinguish PV properties from PVC requests instead of treating both as the same object.

## Reclaiming

When a user is done with a volume, the PVC can be deleted. The reclaim policy tells Kubernetes what to do with the volume after the claim is released.

Current reclaim policy names include:

- Retain.
- Delete.
- Recycle.

The Recycle policy is deprecated. Dynamic provisioning is the recommended approach instead of relying on recycling.

## Retain Reclaim Policy

The Retain reclaim policy allows manual reclamation. When the PVC is deleted, the PV still exists and is considered released, but it is not yet available for another claim because data from the previous claim remains.

Manual reclaim steps:

1. Delete the PersistentVolume.
2. Clean up data on the associated storage asset.
3. Delete the associated storage asset, or create a new PersistentVolume using the same storage asset definition if reuse is desired.

## Delete Reclaim Policy

For volume plugins that support the Delete reclaim policy, deletion removes both the PersistentVolume object from Kubernetes and the associated external storage asset. Dynamically provisioned volumes inherit the reclaim policy of their StorageClass, which defaults to Delete unless configured otherwise.

## PVC Expansion

For in-use PVC expansion, a Pod using the PVC can see the expanded file system once expansion is complete. The user does not normally need to delete and recreate the Pod just because a supported in-use PVC expands.

## Recovering From Failure When Expanding Volumes

If a user requests a size that the underlying storage system cannot satisfy, expansion may be retried until action is taken. A cluster administrator can recover manually:

1. Mark the PersistentVolume bound to the PVC with the Retain reclaim policy.
2. Delete the PVC. With Retain, data is not lost by deleting the PVC.
3. Delete the `claimRef` entry from the PV spec so the PV can become Available.
4. Re-create the PVC with a smaller size than the PV and set `volumeName` to the existing PV name.
5. Restore the reclaim policy of the PV.

Kubernetes does not support shrinking a PVC below its current size.

## Boundary Notes

This sample covers Kubernetes storage objects. It does not describe MySQL InnoDB MVCC, undo log tuning, or database transaction internals.
