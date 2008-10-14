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

grailsHome = Ant.project.properties."environment.GRAILS_HOME"
includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )  

includeTool << gant.tools.Execute

def i18nDir = 'grails-app/i18n'
def configFile = "${basedir}/grails-app/conf/Config.groovy"

includeTargets << gant.targets.Clean
cleanPattern << [  i18nDir+'/*$1.class', i18nDir+'/*.po~', i18nDir+'/*.pot' ]
clobberPattern << [ i18nDir+'/*.class' ]

def getConfigFile = {
	File dsFile = new File( configFile )
	def dsConfig = null
	if (dsFile.exists()) {
		dsConfig = new ConfigSlurper(grailsEnv).parse(dsFile.text)
	}
	return dsConfig
}

target('default': "Scan all .groovy and .gsp files for tr() trn() and merge with all .po files in "+i18nDir ) {

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
		 merge()
		break
		case 'scan':
		default:
		  scan()
	}
}




target ( scan: 'Search all .groovy or .gsp files for tr() trn() and generate a .pot file in grails-app/i18n.' ) {
	println('Generating .pot file from sources.')

	def charset = getConfigFile()?.I18nGettext?.inputFileCharset ?:"UTF-8"

	// trash the last .pot file
	new File( i18nDir+'/keys.pot' ).write("")
	
	new File(".").eachFileRecurse{ file ->
		if( file.isFile() && ( file.name.endsWith("groovy") || file.name.endsWith(".gsp") || file.name.endsWith(".java") || file.name.endsWith(".jsp") ) ){
			Execute.shell( 'xgettext -j --force-po -ktrc -ktr -kmarktr -ktrn:1,2 --from-code '+charset+' -o'+i18nDir+'/keys.pot -Ljava '+file.absolutePath, outProcessing : { } )
		}
	}
	
	merge()
}


target ( merge: 'Merge the .pot file with all available .po files in grails-app/i18n.' ) {
	println('Merging .po files with .pot file.')
	
	touchpo( "Messages" )	// the default Resource
	
	List fl = new File(i18nDir).listFiles([accept:{file->file ==~ /.*?\.po/ }] as FileFilter).toList().name

	fl.each(){
		if( !it.contains('~') ){
			String lang = it.replace( ".po", "" )
	
			Execute.shell( 'msgmerge -U '+i18nDir+'/'+lang+'.po '+i18nDir+'/keys.pot' )
		}
	}
}


target ( makemo: 'Update all .mo files from their updated .po files and write results to WEB-INF/i18ngettext.' ) {
	println('Compiling .mo files.')
	
	List fl = new File(i18nDir).listFiles([accept:{file->file ==~ /.*?\.po/ }] as FileFilter).toList().name
	fl.each(){
		
		if( !it.contains('~') ){
			String lang = it.replace( ".po", "" )
			
			if( lang=="Messages" ){
				Execute.shell( 'msgfmt --java2 -d web-app/WEB-INF/ -r i18ngettext.Messages '+i18nDir+'/Messages.po' )	// the default Resource
			} else {
				Execute.shell( 'msgfmt --java2 -d web-app/WEB-INF/ -r i18ngettext.Messages -l '+lang+' '+i18nDir+'/'+lang+'.po' )
			}
		}
	}
}

target ( touchpo: 'Create a .po file copy from Messages.po (Create Messages.po if it does not exist).' ){

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
	
	if( it ){
		
		it = it.replace( ".po", "" )
		
		def destination = new File( i18nDir+'/'+it+'.po' );
		
		if( !destination.exists() ){
			
			if( it=="Messages" ){

				// write our default header to the file
				destination.write( header, 'UTF-8' )
				
			} else {
				// make sure the "Messages.po" file exists
				touchpo( "Messages" )
				
				def source = new File( i18nDir+'/Messages.po' );
				if( source ){
					// copy the "Messages.po" file to the new name.
					destination.withOutputStream{ out->out.write source.readBytes() }
				} else {
					// write our default header to the file
					destination.write( header, 'UTF-8' )
				}

			}
		}

	}

}


