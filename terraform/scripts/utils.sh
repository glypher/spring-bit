#!/bin/bash

retry_command() {
    local command="$1"  # The full command string
    local max_attempts=4
    local attempt=0
    local success=1

    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        echo "Attempt $attempt: $command"
        eval "$command"  # Execute the command string

        if [ $? -eq 0 ]; then
            success=0
            break
        fi
        sleep 1
    done

    if [ $success -ne 0 ]; then
        echo "Command failed after $max_attempts attempts"
    fi

    return $success
}

print_message_with_envelope() {
    local message="$1"
    local border_length=${#message}
    local border=$(printf "#%.0s" $(seq 1 $((border_length + 4))))

    echo "$border"
    echo "# $message #"
    echo "$border"
}