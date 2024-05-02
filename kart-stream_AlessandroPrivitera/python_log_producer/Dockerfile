FROM python:3.10-slim

WORKDIR /app

COPY . /app

RUN pip install --no-cache-dir -r requirements.txt

ENV URL "http://kart-logs-ingestor:9090"

CMD python log_sender.py ${URL}