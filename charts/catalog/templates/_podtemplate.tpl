{{/*
Шаблон пода: спільний для Deployment і Rollout,
щоб не дублювати опис контейнера.
*/}}
{{- define "catalog.podTemplate" -}}
metadata:
  labels:
    {{- include "catalog.labels" . | nindent 4 }}
  annotations:
    # при зміні ConfigMap/Secret поди перезапускаються автоматично
    checksum/config: {{ include (print $.Template.BasePath "/configmap.yaml") . | sha256sum }}
    {{- if not .Values.existingSecret }}
    checksum/secret: {{ include (print $.Template.BasePath "/secret.yaml") . | sha256sum }}
    {{- end }}
spec:
  containers:
    - name: {{ .Chart.Name }}
      image: "{{ .Values.image.repository }}:{{ include "catalog.imageTag" . }}"
      imagePullPolicy: {{ .Values.image.pullPolicy }}
      ports:
        - name: http
          containerPort: {{ .Values.containerPort }}
      envFrom:
        - configMapRef:
            name: {{ include "catalog.fullname" . }}-config
        - secretRef:
            name: {{ .Values.existingSecret | default (printf "%s-secret" (include "catalog.fullname" .)) }}
      resources:
        {{- toYaml .Values.resources | nindent 8 }}
      livenessProbe:
        httpGet:
          path: {{ .Values.probes.liveness.path }}
          port: http
        initialDelaySeconds: {{ .Values.probes.liveness.initialDelaySeconds }}
        periodSeconds: {{ .Values.probes.liveness.periodSeconds }}
        timeoutSeconds: {{ .Values.probes.liveness.timeoutSeconds }}
        failureThreshold: {{ .Values.probes.liveness.failureThreshold }}
      readinessProbe:
        httpGet:
          path: {{ .Values.probes.readiness.path }}
          port: http
        initialDelaySeconds: {{ .Values.probes.readiness.initialDelaySeconds }}
        periodSeconds: {{ .Values.probes.readiness.periodSeconds }}
        timeoutSeconds: {{ .Values.probes.readiness.timeoutSeconds }}
        failureThreshold: {{ .Values.probes.readiness.failureThreshold }}
      {{- if .Values.persistence.enabled }}
      volumeMounts:
        - name: data
          mountPath: {{ .Values.persistence.mountPath }}
      {{- end }}
  {{- if .Values.persistence.enabled }}
  volumes:
    - name: data
      persistentVolumeClaim:
        claimName: {{ include "catalog.fullname" . }}-data
  {{- end }}
{{- end }}
