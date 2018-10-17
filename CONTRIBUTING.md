# Contributing to Nailgun

We encourage the reporting of issues and bugs, along with pull requests to help make Nailgun codebase better. The following are some information and guidelines to help you contribute to Nailgun.

## Tour of the Codebase

This is a high-level overview of how the Nailgun repository is organized.

### `nailgun-server/`

That is where server side code lives, written in Java. It contains both core code under `src/main` and test code under `src/tests`.

### `nailgun-client/`

Client part of Nailgun, both C and Python versions in appropriate folders.

### `nailgun-examples/`

Some simple implementations of a Nail, helpful to understand how to write server-side code. One can also execute one of those nails for debugging or integration testing.

### `scripts/`

Automation scripts, mostly for continuous integration (i.e. Travis CI).

### `tools/`

Third-party dependencies used in tooling, like linter or code formatter.

## Development Workflow

### Building Nailgun

As simple as running `mvn clean package`.

### Running Tests

Unit tests and integration tests are JUnit and they are run by Maven when you say 'mvn package'. To run E2E test, just execute `./scripts/travis_ci.sh`. It will also run `mvn package` first with all unit tests.

### Using the IntelliJ IDE

Just open project from Nailgun's root folder

### Code Style

Code is autoformatted with Maven plugin and Google code style is used. Be ready to have some changed files after building the project with Maven, so you may have to amend the change to git.
