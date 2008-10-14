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

import java.util.Locale;

class I18nGettextTagLibTests extends GroovyTestCase {
	
	I18nGettextTagLib tl = null
	
	void setUp(){
    	tl = new I18nGettextTagLib()
    	tl.session.setAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE", "en")
	}
	
	
	
    void testTrAvailability() {
    	assert tl.tr()=="en"
    }
    
    void testTrAvailabilityWithForcedLocale() {
    	tl.session.setAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE", "de")
    	assert tl.tr()=="de"
    }
    
    
    
    void testTr(){
    	assert tl.tr("foo")=="foo"
       	assert tl.tr("foo{0}",42)=="foo42"
      	assert tl.tr("foo{0} and {1}", [42,"bar"])=="foo42 and bar"
    }
    
    void testTrWithForcedLocale(){
    	assert tl.tr("foo", "de")=="schnick"
       	assert tl.tr("foo{0}",42, "de")=="schnick42"
      	assert tl.tr("foo{0} and {1}", [42,"bar"], "de")=="schnick42 und bar"
    }
    
    
    
    void testTrn(){
    	assert tl.trn("foo", "foos", 1) == "foo"
    	assert tl.trn("foo", "foos", 2) == "foos"
    	assert tl.trn("foo{0}", "foos{1}", 1, ["bar", "bars", "baz", "bazs"] ) == "foobar"
    	assert tl.trn("foo{2}", "foos{3}", 1, ["bar", "bars", "baz", "bazs"] ) == "foobaz"
    	assert tl.trn("foo{0}", "foos{1}", 2, ["bar", "bars", "baz", "bazs"] ) == "foosbars"
    	assert tl.trn("foo{2}", "foos{3}", 2, ["bar", "bars", "baz", "bazs"] ) == "foosbazs"
    }
    
    void testTrnWithForcedLocale(){
    	assert tl.trn("foo", "foos", 1, "de") == "schnick"
    	assert tl.trn("foo", "foos", 2, "de") == "schnicks"
    	assert tl.trn("foo{0}", "foos{1}", 1, ["bar", "bars", "baz", "bazs"], "de" ) == "schnickbar"
    	assert tl.trn("foo{2}", "foos{3}", 1, ["bar", "bars", "baz", "bazs"], "de" ) == "schnickbaz"
    	assert tl.trn("foo{0}", "foos{1}", 2, ["bar", "bars", "baz", "bazs"], "de" ) == "schnicksbars"
    	assert tl.trn("foo{2}", "foos{3}", 2, ["bar", "bars", "baz", "bazs"], "de" ) == "schnicksbazs"
    }
    
    
    
    void testTrc(){
    	assert tl.trc("fish (verb)", "fish") == "fish"
    	assert tl.trc("fish (noun)", "fish") == "fish"
    }
    
    void testTrcWithForcedLocale(){
    	assert tl.trc("fish (verb)", "fish", "de", "en") == "angeln"
    	assert tl.trc("fish (noun)", "fish", "de", "en") == "Fisch"
    	
    	assert tl.trc("fish (verb)", "fish", "en", "en") == "fish"
    	assert tl.trc("fish (noun)", "fish", "en", "en") == "fish"
    }
    
    
    
    void testMarkTr(){
    	assert tl.marktr("foomark") == "foomark"
    }

    void testMarkTrWithForcedLocale(){
    	assert tl.marktr("foomark", "de" ) == "foomark"
    }
    
    
}
