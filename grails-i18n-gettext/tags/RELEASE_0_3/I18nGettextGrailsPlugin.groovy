//
//   Copyright 2008 Rainer Brang
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
   
import java.text.MessageFormat
import java.util.Locale
import org.xnap.commons.i18n.*
import org.codehaus.groovy.grails.commons.ApplicationHolder

class I18nGettextGrailsPlugin {
    def observe = ['*']
	
    def version = 0.3
    def dependsOn = [:]

    def author = "Rainer Brang, Backend-Server GmbH & Co. KG"
    def authorEmail = "grails@backend-server.de"
    def title = "I18n gettext plugin for grails"
    def description = """This plugin adds i18n support to your app, in 'gnu gettext'-style.
1 First, you need to wrap special method calls around all strings you want to translate.
2 Then you use an included Gant script to extract all translatable strings from your sources.
3 Now you translate all strings from step 2 which you will find in .po files in your i18n directory.
4 Use another included Gant script to compile your translated .po files into resource classes.
5 repeat 1-4 each time you added some new strings to your application. Existing translations will be merged in. 

During runtime: The methods, you wrapped around the strings, will pick the correct translation according to the
current locale, and return the translated string. You may also force a locale for a specific call.

What you need: The developer needs these command line tools for the development machine: xgettext, msgmerge and msgfmt
The translator may like: PoEdit or alike to translate texts.

You will love the dead simple plural form handling and FormatMessage-like String handling. Additionally, you can 
forget about inventing lookup keys for your .properties files, because for gnu gettext, the original string is the key.

Beware: Gnu gettext can not handle groovy's "here-doc" strings.
"""

    // URL to the plugin's documentation
    def documentation = "http://www.grails.org/I18n-gettext+Plugin"

    def doWithDynamicMethods = { ctx ->

	    application.controllerClasses.each { controllerClass ->
	    	processClass( controllerClass, log )
	    }    	
	    
	    application.domainClasses.each { domainClass ->
	    	processClass( domainClass, log )
	    }    	
	    
	    application.tagLibClasses.each { tagLibClass ->
	    
			if( tagLibClass.name=="I18nGettext" ){
		    	processClass( tagLibClass, log )
			}
	    }    	
	    
    }
    
    def onChange = { event ->
        // Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.

        if ( application.isControllerClass(event.source) ) {
            def controllerClass = application.getControllerClass(event.source?.name)
            processClass( controllerClass, log )
        }

        if ( application.isDomainClass(event.source) ) {
            def domainClass = application.getDomainClass(event.source?.name)
            processClass( domainClass, log )
        }

        if ( application.isTagLibClass(event.source) ) {
            def tagLibClass = application.getTagLibClass(event.source?.name)
            
			if( tagLibClass.name=="I18nGettext" ){
		    	processClass( tagLibClass, log )
			}
        }

    }

    
    def processClass( theClass, log ){
    	
    	log.debug( "processing class: "+theClass.name ) 
    	
    	//  Returns the currently selected locale
       	theClass.metaClass.tr = {->
    		return getCurrentLocale( session, request )
		}
    	
    	
    	// Returns the plural form for n of the translation of text.
    	theClass.metaClass.trn = {String single, String plural, int number, Object messages, String locale -> 
    		def i18n = getI18nObject( session, request, log, locale )
			i18n?i18n.trn( single, plural, number, (Object[])messages ):number>1?MessageFormat.format(plural, (Object[])messages):MessageFormat.format(single, (Object[])messages)
		}

    	theClass.metaClass.trn = {String single, String plural, int number, Object messages -> 
    		def i18n = getI18nObject( session, request, log )
			i18n?i18n.trn( single, plural, number, (Object[])messages ):number>1?MessageFormat.format(plural, (Object[])messages):MessageFormat.format(single, (Object[])messages)
		}

    	
    	// Returns the plural form for n of the translation of text.
    	theClass.metaClass.trn = {String single, String plural, int number, String locale -> 
    		def i18n = getI18nObject( session, request, log, locale )
			i18n?i18n.trn( single, plural, number ):number>1?plural:single
		}

    	theClass.metaClass.trn = {String single, String plural, int number -> 
    		def i18n = getI18nObject( session, request, log )
			i18n?i18n.trn( single, plural, number ):number>1?plural:single
		}

    	
    	//  Returns text translated into the currently selected language
    	theClass.metaClass.tr = {String single, Object messages, String locale ->
    		def i18n = getI18nObject( session, request, log, locale )
   			i18n?i18n.tr(single, (Object[])messages):MessageFormat.format(single, (Object[])messages)
		}    	

    	theClass.metaClass.tr = {String single, Object messages ->
    		def i18n = getI18nObject( session, request, log )
   			i18n?i18n.tr(single, (Object[])messages):MessageFormat.format(single, (Object[])messages)
		}    	

    	
    	//  Returns text translated into the currently selected language
    	theClass.metaClass.tr = {String single, String locale ->
    		def i18n = getI18nObject( session, request, log, locale )
   			i18n?i18n.tr(single):single
		}    	

    	theClass.metaClass.tr = {String single ->
    		def i18n = getI18nObject( session, request, log )
   			i18n?i18n.tr(single):single
		}    	

    	
    	// Disambiguates translation keys
    	theClass.metaClass.trc = {String comment, String text, String locale, String sourceCodeLocale ->
			def i18n = getI18nObject( session, request, log, locale, sourceCodeLocale )
			i18n?i18n.trc(comment, text):text
    	}

    	theClass.metaClass.trc = {String comment, String text, String locale ->
			def i18n = getI18nObject( session, request, log, locale )
			i18n?i18n.trc(comment, text):text
    	}

    	theClass.metaClass.trc = {String comment, String text ->
    		def i18n = getI18nObject( session, request, log )
			i18n?i18n.trc(comment, text):text
		}    	

    	
    	// Marks text to be translated, but doesn't return the translation but text itself.
    	theClass.metaClass.marktr = {String text, String locale ->
    		def i18n = getI18nObject( session, request, log, locale )
			i18n?i18n.marktr(text):text
		}    	

    	theClass.metaClass.marktr = {String text ->
    		def i18n = getI18nObject( session, request, log )
			i18n?i18n.marktr(text):text
		}    	

    }
    

    
    /**
     * Get an I18n from the I81nFactoy and set the current locale and source code locale.
     */
	public getI18nObject( session, request, log, wantLocale=null, forceSourceCodeLocale=null ) {
    	
		def i18n = null
		try{
			
			def language = ""
			def country = ""
			def variant = ""

			// use locale string forced by the method call or from the session.
			if ( !wantLocale ){
				wantLocale = getCurrentLocale( session, request )
			}
			def wantedLocale = null
			try{
				language = wantLocale?.split("_")[0].toLowerCase()
				country = wantLocale?.split("_")[1].toUpperCase()
				variant = wantLocale?.split("_")[2]
			} catch( ArrayIndexOutOfBoundsException){
				// ignore
			}
			wantedLocale = new Locale( language, country, variant )
			
			
			// use source code locale string forced by the method call or from config or use the bailout "en"
			if ( !forceSourceCodeLocale ){
				forceSourceCodeLocale = ApplicationHolder?.application?.config?.I18nGettext?.sourceCodeLocale ?:"en"
			}
			def wantedSourceCodeLocale = null
			language = ""
			country = ""
			variant = ""
			try{
				language = forceSourceCodeLocale?.split("_")[0].toLowerCase()
				country = forceSourceCodeLocale?.split("_")[1].toUpperCase()
				variant = forceSourceCodeLocale?.split("_")[2]
			} catch( ArrayIndexOutOfBoundsException){
				// ignore
			}
			wantedSourceCodeLocale = new Locale( language, country, variant )
			

			i18n = I18nFactory.getI18n( I18nGettextGrailsPlugin.class, "i18ngettext.Messages" )
			i18n.setLocale( wantedLocale )
			i18n.setSourceCodeLocale( wantedSourceCodeLocale )
			
		} catch( MissingResourceException mre ){
			log.error( mre.getMessage()+". Key: "+mre.getKey()+" Class: "+mre.getClassName() )
			return null
		}
		
		return i18n
	}
	
	
	/**
	 * Get the current locale - either from the session, or from the browser's language
	 */
	public getCurrentLocale( session, request ){
		
		def currentLocale = session?.getAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE")
		currentLocale = currentLocale?currentLocale:request?.getLocale()
				
		// Fallbacks		
		if( !currentLocale ){
			currentLocale = ApplicationHolder?.application?.config?.I18nGettext?.sourceCodeLocale ?:"en"			
		}
		
		return currentLocale.toString()		
	}
	
	
}
