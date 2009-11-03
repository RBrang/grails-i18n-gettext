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

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

grailsHome = ant.project.properties."environment.GRAILS_HOME"

includeTargets << grailsScript("Init")
includeTool << gant.tools.Execute

includeTargets << gant.targets.Clean
def configFile = "${i18nGettextPluginDir}/grails-app/conf/Config.groovy"
def i18nDir = "./grails-app/i18n"
def i18nOutputDir = "${i18nGettextPluginDir}/lib"

def getConfigFile = {
    File dsFile = new File( configFile )
    def dsConfig = null
    if( dsFile.exists() ) {
    	dsConfig = new ConfigSlurper(grailsEnv).parse(dsFile.text)
    }
    return dsConfig
}

target( 'default': "Scan all .groovy and .gsp files for tr() trn() and merge with all .po files in "+i18nDir ) {
        parameters = []

        if( args ){
        	parameters = args.split("\n")
        }

        switch( parameters[0] ){
        case 'init':
          touchpo( parameters[1] )
        break
        case 'clobber':
         clobber()
        break
        case 'clean':
         clean()
        break
        case 'makemo':
         makemo()
        break
        case 'merge':
        	mergepo()
        break
        case 'touchpo':
        	touchpo( parameters[1] )
        break
        case 'scan':
        default:
        	scan()
    }
}



scan = {
		
    println("\nGenerating .pot file from sources.")

    def charset = getConfigFile()?.I18nGettext?.inputFileCharset ?:"UTF-8"

    // trash the last .pot file
    def keysFileName = i18nDir+"/keys.pot"
    new File( keysFileName ).write("")

    def excludedDirsArray = getConfigFile()?.I18nGettext?.excludedDirsArray ?:["scripts"]
    
    new File(".").eachFileRecurse{ file ->
    	if (!excludedDirsArray.any {file.path.contains(it)}){
    		
            if( file.isFile() ){
                // switch programming language identifier for best recognition rates
                def programmingLanguageIdentifier = ""
                if( file.name.endsWith(".groovy") || file.name.endsWith(".java") ){
                    programmingLanguageIdentifier = "java"
                } else if( file.name.endsWith(".gsp") || file.name.endsWith(".jsp") ) {
                    // pretend to scan a .php file, which results in a much higher recognition rate.
                    programmingLanguageIdentifier = "php"
                } 
                    	
                if( programmingLanguageIdentifier.length()>0 ){
                    def command = 'xgettext -j --force-po -ktrc -ktr -kmarktr -ktrn:1,2 --from-code  '+charset+' -o '+i18nDir+'/keys.pot -L'+programmingLanguageIdentifier+' '+file.getCanonicalPath()
                    
                    println( command )
                    def e = command.execute()
                    e.waitFor()
                    if( e.exitValue() ){
                        println( "Error: "+e.err.text )
                    }
                }
            }
    	}
    }

    mergepo()
}



mergepo = {

	println("\nMerging .po files with .pot file.")
    touchpo( "Messages" )        // the default Resource

    List fl = new File(i18nDir).listFiles([accept:{file->file ==~ /.*?\.po/ }] as FileFilter).toList().name

    fl.each(){
        if( !it.contains('~') ){
            String lang = it.replace( ".po", "" )

            command = 'msgmerge -U '+i18nDir+'/'+lang+'.po '+i18nDir+'/keys.pot'
            println( command )
            def e = command.execute()
            e.waitFor()
            if( e.exitValue() ){
                println( "Error: "+e.err.text )
            }
        }
    }
}


makemo = {
    println("\nCompiling .mo files.")

    def destination = new File( i18nOutputDir );
    if( !destination.exists() ){
        destination.mkdir()
    }
    
    def i18nOutputDirCanonical = destination.getCanonicalPath()

    List fl = new File(i18nDir).listFiles([accept:{file->file ==~ /.*?\.po/ }] as FileFilter).toList().name
    fl.each(){
        if( !it.contains('~') ){
            String lang = it.replace( ".po", "" )

            if( lang=="Messages" ){
                command = 'msgfmt --java2 -d '+i18nOutputDirCanonical+' -r i18ngettext.Messages '+i18nDir+'/'+messageFileName  // the default Resource
            } else {
                command = 'msgfmt --java2 -d '+i18nOutputDirCanonical+' -r i18ngettext.Messages -l '+lang+' '+i18nDir+'/'+lang+'.po'
            }

            println( command )
            def e = command.execute()
            e.waitFor()
            if( e.exitValue() ){
                println( "Error: "+e.err.text )
            }
        }
    }
    
    ant.jar( basedir:"${i18nOutputDirCanonical}", includes:"i18ngettext/*", destfile:"./lib/i18ngettext.jar")
}


touchpo = { fileNameToCreate->

    def charset = getConfigFile()?.I18nGettext?.inputFileCharset ?:"UTF-8"
    def header = """
# SOME DESCRIPTIVE TITLE.
# Copyright (C) YEAR AUTHOR
# This file is distributed under the same license as the PACKAGE package.
# FIRST AUTHOR <EMAIL@ADDRESS>, YEAR.
#
#, fuzzy
msgid ""
msgstr ""
"Project-Id-Version: PACKAGE VERSION\\n"
"Report-Msgid-Bugs-To: \\n"
"POT-Creation-Date: YEAR-MO-DA HO:MO+ZONE\\n"
"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n"
"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n"
"Language-Team: LANGUAGE <LL@li.org>\\n"
"MIME-Version: 1.0\\n"
"Content-Type: text/plain; charset=${charset}\\n"
"Content-Transfer-Encoding: 8bit\\n"
"""

	if( fileNameToCreate.length()>0 ){
		
        fileNameToCreate = fileNameToCreate.replace( ".po", "" )

        def destination = new File( ""+i18nDir+'/'+fileNameToCreate+'.po' );

        if( destination.exists() ){
        	if( fileNameToCreate != "Messages" ){
            	println( "File: "+destination.getCanonicalPath()+" already exists. Will not recreate it.")
        	}
        } else {
            if( fileNameToCreate=="Messages" ){
            	// write our default header to the file
            	destination.write( header, 'UTF-8' )
            } else {
                // make sure the "Messages.po" file exists
                touchpo( "Messages" )

                def source = new File( ""+i18nDir+'/Messages.po' );
                if( source ){
                    // copy the "Messages.po" file to the new name.
                    destination.withOutputStream{ out->out.write source.readBytes() }
                } else {
                    // write our default header to the file
                    destination.write( header, 'UTF-8' )
                }
            	println( "File: "+destination.getCanonicalPath()+" has been created.")
            }
        }
    }
}
