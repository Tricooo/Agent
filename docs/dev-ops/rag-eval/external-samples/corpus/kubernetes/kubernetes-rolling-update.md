# External RAG Sample: Kubernetes Rolling Update

Source URL: https://kubernetes.io/docs/tasks/run-application/update-deployment-rolling/
Prepared for: operational workflow, trigger conditions, paraphrase tests.
Snapshot note: compact offline sample prepared on 2026-05-23 from Kubernetes rolling update tutorial content.

## Objective

A rolling update updates a running Kubernetes Deployment to a new version while reducing downtime risk. It gradually replaces old Pods with new Pods so the application can remain available during the transition.

The tutorial objectives include:

- Trigger a rolling update on a Deployment.
- Monitor rollout progress.
- Pause and resume the rollout.
- Configure rolling update strategy parameters.
- Roll back to a previous revision when required.

## Trigger

Any change to the `.spec.template` field of a Deployment triggers a rolling update. This includes changes to the Pod template such as container image, labels inside the template, or other Pod specification fields.

The key literal expected point is `.spec.template`.

## Availability Reasoning

Rolling updates reduce downtime risk by replacing Pods gradually rather than deleting all existing Pods first. During the transition, some old Pods can continue serving while new Pods start and become ready. The process can be monitored, paused, resumed, and rolled back.

This does not guarantee zero downtime for every application. Readiness probes, capacity, update strategy parameters, and application behavior still matter.

## Boundary Notes

This sample explains Deployment rolling updates. It does not define the full Kubernetes storage lifecycle, and it does not explain database migration strategy beyond the deployment-update concept.
