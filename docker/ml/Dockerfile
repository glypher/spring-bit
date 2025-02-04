FROM python:3.13-slim AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends unzip curl
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

RUN curl -s https://api.github.com/repos/grafana/loki/releases/latest | \
        grep browser_download_url | \
        cut -d '"' -f 4 | grep promtail-linux-amd64.zip | \
        xargs curl -O -L

RUN unzip promtail-linux-amd64.zip
RUN mv promtail-linux-amd64 /usr/local/bin/promtail


FROM python:3.13-slim
WORKDIR /app

# Copy only necessary files from builder stage
COPY --from=builder /opt/venv /opt/venv
COPY --from=builder /usr/local/bin/promtail /usr/local/bin/.
COPY config/promtail-config.yml /etc/promtail/.
COPY config/start.sh /usr/local/bin/.
RUN chmod a+x /usr/local/bin/promtail && chmod a+x /usr/local/bin/start.sh

COPY config/ /app/config
COPY crypto/ /app/crypto
COPY main.py /app

# Use the same virtual environment
ENV PATH="/opt/venv/bin:$PATH"
ENV ENV=production


RUN groupadd springbit
RUN useradd -r -g springbit springbit
RUN useradd -r -g springbit promtail

RUN mkdir -p /var/log/springbit && chown -R springbit:springbit /var/log/springbit

# Expose the port that FastAPI will run on (default is 8000)
EXPOSE 8000

ENTRYPOINT ["start.sh"]

# Command to run the FastAPI app using Uvicorn (with host set to 0.0.0.0 to allow external access)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
