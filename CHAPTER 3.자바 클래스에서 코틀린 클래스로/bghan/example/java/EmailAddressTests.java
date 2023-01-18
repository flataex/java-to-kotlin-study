public class EmailAddressTests {

    @Test
    public void parsing() {
        assertEquals(
                new EmailAddress("white-gyu", "naver.com"),
                EmailAddress.parse("white-gyu@naver.com")
        );
    }

    @Test
    public void parsingFailures() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailAddress.parse("@")
        );
    }
}