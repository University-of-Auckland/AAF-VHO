import javax.naming.InitialContext
import javax.naming.Context

import grails.util.Environment
import org.apache.log4j.FileAppender

// Import externalized configuration
if(Environment.current != Environment.TEST) {
  def externalConf = getFromEnvironment("config_dir")
  if(externalConf) {
    grails.config.locations = ["file:${externalConf}/application_config.groovy"]
  } else {
    println "No external configuration location specified as environment variable config_dir, terminating startup"
    throw new RuntimeException("No external configuration location specified as environment variable config_dir")
  }
}

// Extract user details to append to Audit Table
auditLog {
  actorClosure = { request, session ->
    org.apache.shiro.SecurityUtils.getSubject()?.getPrincipal()
  }
}

security.shiro.authc.required = false
security.shiro.authc.strategy = aaf.base.shiro.FirstExceptionStrategy

// Enable console plugin
grails.plugin.console.enabled = true

grails.project.groupId = appName
grails.converters.xml.pretty.print = true
grails.mime.file.extensions = true
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html', 'application/xhtml+xml'],
  xml: ['text/xml', 'application/xml'],
  text: 'text/plain',
  js: 'text/javascript',
  rss: 'application/rss+xml',
  atom: 'application/atom+xml',
  css: 'text/css',
  csv: 'text/csv',
  all: '*/*',
  json: ['application/json', 'text/json'],
  form: 'application/x-www-form-urlencoded',
  multipartForm: 'multipart/form-data'
]

grails.views.default.codec = "HTML"
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

grails.views.gsp.sitemesh.preprocess = true
grails.scaffolding.templates.domainSuffix = 'Instance'
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

grails.json.legacy.builder = false
grails.enable.native2ascii = true
grails.spring.bean.packages = []
grails.web.disable.multipart=false

grails.exceptionresolver.params.exclude = ['password', 'password_confim']

environments {
  test {
    testDataConfig.enabled = true
    
    grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
    grails.mail.default.from="noreply-test@aaf.edu.au"
    greenmail.disabled = false

    log4j = {
      appenders {
        appender new FileAppender(name:"test-output", layout:pattern(conversionPattern: "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n"), file:"/tmp/app-test-output.log")
      }
      warn 'test-output'     :[ 'grails.buildtestdata'], additivity:false
      info  'test-output'    :[ 'grails.app.controllers',
                                'grails.app.domains',
                                'grails.app.services',
                                'grails.app.realms',
                                'aaf.vhr']
    }

    jasypt {
      algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC"
      providerName = "BC"
      password = "mesecretivesecretmehearties"
      keyObtentionIterations = 100
    }
  }
}

/**
* This is allows usage of environment variables in production
* while maintaining flexibility in development.
*/
public String getFromEnvironment(final String name) {
  if(name == null) return null;
  try {
    final Object object = ((Context)(new InitialContext().lookup("java:comp/env"))).lookup(name);
    if (object != null)
      return object.toString();
  } catch (final Exception e) {}

  System.getenv(name);
}

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
