Nailgun Maven Central deployment process
=======

- Create Sonatype Jira account

https://central.sonatype.org/pages/ossrh-guide.html

- Install gpg

brew install gpg

- Generate gpg key

gpg --gen-key
Follow instructions

- Set key expiration to forever
https://www.g-loaded.eu/2010/11/01/change-expiration-date-gpg-key/

- Upload public key
gpg --list-keys
gpg --keyserver pgp.mit.edu --send-keys <key_id>

- Fix gpg terminal, otherwise Maven plugin errors

GPG_TTY=$(tty)
EXPORT GPG_TTY

- Create private Maven settings by adding the following to ~/.m2/settings.xml

<settings>
  <servers>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>sonatype_username</username>
      <password>sonatype_password</password>
    </server>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>sonatype_username</username>
      <password>sonatype_password</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>release-sign-artifacts</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>gpg_passphrase</gpg.passphrase>
        <gpg.useagent>true</gpg.useagent>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>release-sign-artifacts</activeProfile>
  </activeProfiles>
</settings>

- Modify all pom.xml files to exclude `-SNAPSHOT` from version

- Run deploy with Maven

mvn clean deploy -Prelease-sign-artifacts

- Promote release to production

Login to https://oss.sonatype.org, navigate to 'Staging Repository', find the newly created
repository and press 'Close', wait for operation to complete, then press 'Release'.
It will promote repository from staging to Central.