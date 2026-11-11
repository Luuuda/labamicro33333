{{/*
Коротке ім'я чарта.
*/}}
{{- define "catalog.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Повне ім'я об'єктів: будується з імені релізу,
тому чарт можна встановити кілька разів в один кластер.
*/}}
{{- define "catalog.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Ім'я та версія чарта для мітки helm.sh/chart.
*/}}
{{- define "catalog.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Мітки для selector (не змінюються між оновленнями).
*/}}
{{- define "catalog.selectorLabels" -}}
app.kubernetes.io/name: {{ include "catalog.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Повний набір міток для всіх об'єктів.
*/}}
{{- define "catalog.labels" -}}
helm.sh/chart: {{ include "catalog.chart" . }}
{{ include "catalog.selectorLabels" . }}
app.kubernetes.io/version: {{ include "catalog.imageTag" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Тег образу: з values, або appVersion з Chart.yaml.
*/}}
{{- define "catalog.imageTag" -}}
{{- .Values.image.tag | default .Chart.AppVersion }}
{{- end }}
