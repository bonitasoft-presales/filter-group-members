import groovy.xml.MarkupBuilder

def xml = new StringWriter()
def builder = new MarkupBuilder(xml)
builder.jarDependencies {
    jarDependency("${project.artifactId}-${project.version}.${project.packaging}")
    project.artifacts
            .findAll { artifact -> artifact.scope == "compile" || artifact.scope == "runtime" }
            .sort { artifact -> artifact.artifactId }
            .each { artifact ->
                jarDependency(artifact.classifier ? "${artifact.artifactId}-${artifact.version}-${artifact.classifier}.${artifact.type}" 
                                                  : "${artifact.artifactId}-${artifact.version}.${artifact.type}")
            }
}
def deps = xml.toString()
project.properties.setProperty("impl-dependencies", deps)