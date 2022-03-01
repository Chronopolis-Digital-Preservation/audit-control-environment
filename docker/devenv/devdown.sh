#!/bin/bash
echo "############################################ Shutting down dev environment."
docker-compose --project-name ace down
echo "############################################ Dev env down."
