grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.source.level = 1.7
grails.project.target.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// uncomment to disable ehcache
		// excludes 'ehcache'
	}
	log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve

	repositories {
		inherits true // Whether to inherit repository definitions from plugins
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenCentral()
		
		// uncomment these to enable remote dependency resolution from public Maven repositories
		//mavenCentral()
		//mavenLocal()
		//mavenRepo "http://snapshots.repository.codehaus.org"
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
	}
	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
	}

	plugins {
        // plugins for the build system only
        compile ":tomcat:7.0.54"
        compile ":hibernate:3.6.10.17" //        compile ":hibernate4:4.3.5.5"
		compile ":scoped-proxy:0.3"
		build ":release:3.1.1"		
	}
}
