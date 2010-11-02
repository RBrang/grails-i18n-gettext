t9n{
	inputFileCharset = "UTF-8"				// charset of all input files
    sourceCodeLocale = "en"					// which is the original locale?
    noWrapPoLines = false					// use -no-wrap parameter when creating/updating .po files

    excludedDirsArray = ["target", "scripts", "docs", "bin", "lib" ]	// exclude these directories from scanning for translatable strings. 
    										// (entries are relative to your project root)
}
