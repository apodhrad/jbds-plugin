# JBDS Maven Plugin
This plugin helps to download and install JBDS / Eclipse. You can also specify which features will be installed into it.

## Enable

```xml
<repositories>
  <repository>
    <id>jboss-apodhrad-group</id>
    <url>http://nexus-apodhrad.rhcloud.com/nexus/content/groups/public</url>
    <layout>default</layout>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
    <id>jboss-apodhrad-group</id>
    <url>http://nexus-apodhrad.rhcloud.com/nexus/content/groups/public</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </pluginRepository>
</pluginRepositories>
````
