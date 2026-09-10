# disposer-fee-and-pay

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Note: Docker Compose V2 is highly recommended for building and running the application.
In the Compose V2 old `docker-compose` command is replaced with `docker compose`.

Create docker image:

```bash
  docker compose build
```

Run the distribution (created in `build/install/disposer-fee-and-pay` directory)
by executing the following command:

```bash
  docker compose up
```

This will start the API container exposing the application's port
(set to `4550` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Operational runbook

### Daily schedule

The service is deployed as a Kubernetes CronJob. The default chart schedule is daily:

```yaml
job:
  schedule: "0 22 * * *"
  concurrencyPolicy: Forbid
```

AAT and production schedules are configured in `cnp-flux-config`:

- `apps/disposer/disposer-fee-and-pay/aat.yaml`
- `apps/disposer/disposer-fee-and-pay/prod.yaml`

`concurrencyPolicy: Forbid` prevents overlapping scheduled runs from the CronJob controller.

### DISPOSER_FEE_PAY_ENABLED

The disposer is set to false by default in application.yaml.

```yaml
service:
  enabled: ${DISPOSER_FEE_PAY_ENABLED:false}
```

### Manual run

```bash
./bin/manual-run-disposer.sh
```

The script defaults to:

- namespace: `disposer`
- CronJob: `disposer-fee-and-pay-job`

Override those values when needed:

```bash
NAMESPACE=disposer CRONJOB=disposer-fee-and-pay ./bin/manual-run-disposer.sh
```
The script creates a Job from the CronJob only when no active `disposer-fee-and-pay` job is already running. 
If an active job exists it logs a blocked manual run message and exits without creating another job.
If the manual disposer is triggered twice and the disposer has already processed historical cases then the system will skip already-deleted cases and logs this as a non-action.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
