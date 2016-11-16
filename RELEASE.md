
Steps for Releasing
===================

(TODO: Hopefully later, we can write some tasks to automate this stuff :-)

1. Run `gradlew build` to make sure we have a successful build.

2. Update the `gradle.properties` file to release version (should only need to remove `SNAPSHOT`).

3. Update examples to use release version (this will need to be automated soon).

4. Update README to use release version.

5. Update CHANGELOG.

6. Uncomment `dryRun = true` in the `bintray` closure. We should make a dry run just to make
sure everything works.

7. Do dry run

        gradlew bintrayUpload \
            -PbintrayUser=<user> \
            -PbintrayApiKey=<apiKey> \
            -PgpgPassphrase=<passphrase> \
            -PossrhUser=<ossrhUser> \
            -PossrhPassword=<ossrhPass>

8. If all goes well, comment out the `dryRun` and commit changes.

        git commit -m 'Prepare for release <version>'

11. Run the command from step 7

10. Add tag for GitHub and push

        git tag -a v<version> -m "RESTDocsEXT Jersey <version>"

11. Change `gradle.properties` to next to next SNAPSHOT and push