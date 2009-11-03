class T9nServiceTests extends GroovyTestCase{
	
	def transactional = false
	def t9nService

	protected void setUp() {
        super.setUp()    }

    protected void tearDown() {
        super.tearDown()
    }
	
    void testTrAvailability() {
    	assert !t9nService.getCurrentLocale().equals("")
    }

}
