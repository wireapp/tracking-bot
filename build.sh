#!/usr/bin/env bash
docker build -t eu.gcr.io/wire-bot/tracking-bot:latest .
docker push eu.gcr.io/wire-bot/tracking-bot
kubectl delete pod -l name=verified
kubectl get pods -l name=verified

