FROM python:3.13-slim

WORKDIR /app

# This will ensure Python outputs all logs to stdout/stderr and sets the application to production mode
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV ENV=production

# Copy the rest of the application code into the container at /app
COPY . /app/

# Install the Python dependencies from requirements.txt
RUN pip install --no-cache-dir -r requirements.txt


# Expose the port that FastAPI will run on (default is 8000)
EXPOSE 8000

# Command to run the FastAPI app using Uvicorn (with host set to 0.0.0.0 to allow external access)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
