import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class T
{
	public static void main(String[] args)
	{
		List<String> list = Arrays.asList(
				"AALIYAH", "AAREN", "AARON", "ABBEY", "ABBI", "ABBIE", "ABBY", "ABE", "ABEGAIL", "ABEL", "ABIGAIL", "ABIGAYLE", "ABNER", "ABRAHAM", "ABRAM", "ACACIA", "ACE", "ADA", "ADAIR", "ADALYN", "ADALYNN", "ADAM", "ADAMINA", "ADDIE", "ADDISON", "ADDY", "ADDYSON", "ADELA", "ADELAIDE", "ADELE", "ADELIA", "ADELINE", "ADELLA", "ADELLE", "ADELYN", "ADEN", "ADOLPH", "ADRIA", "ADRIAN", "ADRIANA", "ADRIANNA", "ADRIANNE", "AGATHA", "AGGIE", "AGNES", "AIDAN", "AIDEN", "AILEEN", "AINSLEE", "AINSLEY", "AINSLIE", "AL", "ALAINA", "ALAN", "ALANA", "ALANIS", "ALANNA", "ALANNAH", "ALANNIS", "ALAYNA", "ALBAN", "ALBERT", "ALBERTA", "ALBIN", "ALDEN", "ALDOUS", "ALEA", "ALEAH", "ALEASE", "ALEC", "ALECIA", "ALEESHA", "ALENE", "ALESHA", "ALESIA", "ALETA", "ALETHA", "ALETHEA", "ALEX", "ALEXA", "ALEXANDER", "ALEXANDRA", "ALEXANDREA", "ALEXANDRIA", "ALEXANDRINA", "ALEXIA", "ALEXINA", "ALEXIS", "ALEXUS", "ALF", "ALFIE", "ALFRED", "ALFREDA", "ALGAR", "ALGER", "ALGERNON", "ALI", "ALIAH", "ALICE", "ALICIA", "ALINE", "ALISE", "ALISHA", "ALISHIA", "ALISIA", "ALISON", "ALISSA", "ALISYA", "ALITA", "ALIVIA", "ALLAN", "ALLANA", "ALLANNAH", "ALLEGRA", "ALLEGRIA", "ALLEN", "ALLIE", "ALLISON", "ALLISSA", "ALLY", "ALLYCIA", "ALLYN", "ALLYSON", "ALMA", "ALOYSIUS", "ALPHA", "ALPHONSO", "ALPHONZO", "ALTON", "ALVA", "ALVENA", "ALVIN", "ALVINA", "ALYCE", "ALYCIA", "ALYS", "ALYSA", "ALYSE", "ALYSHA", "ALYSIA", "ALYSON", "ALYSSA", "ALYSSIA", "ALYX", "AMABEL", "AMANDA", "AMBER", "AMBERLY", "AMBROSE", "AMBROSINE", "AMELIA", "AMERICA", "AMERY", "AMETHYST", "AMI", "AMIAS", "AMIE", "AMILIA", "AMITY", "AMOS", "AMY", "AMYAS", "ANABELLA", "ANABELLE", "ANASTACIA", "ANASTASIA", "ANDERSON", "ANDI", "ANDIE", "ANDRA", "ANDRE", "ANDREA", "ANDREW", "ANDRINA", "ANDY", "ANEMONE", "ANGEL", "ANGELA", "ANGELIA", "ANGELICA", "ANGELINA", "ANGELLE", "ANGIE", "ANGUS", "ANIMA", "ANISE", "ANISSA", "ANITA", "ANIYA", "ANIYAH", "ANJELICA", "ANN", "ANNA", "ANNABEL", "ANNABELLA", "ANNABELLE", "ANNABETH", "ANNALEE", "ANNALISE", "ANNE", "ANNEKA", "ANNETTE", "ANNICE", "ANNIE", "ANNIKA", "ANNIS", "ANNMARIE", "ANNORA", "ANONA", "ANSEL", "ANSELM", "ANSLEY", "ANSON", "ANTHONY", "ANTONETTE", "ANTONIA", "ANTONY", "APRIL", "ARABELLA", "ARAMINTA", "ARCHER", "ARCHIBALD", "ARCHIE", "ARDEN", "ARETHA", "ARIA", "ARIANA", "ARIC", "ARIEL", "ARIELLA", "ARIENNE", "ARIN", "ARLEEN", "ARLEN", "ARLENE", "ARLIE", "ARLINE", "ARLO", "ARN", "ARNIE", "ARNOLD", "ARRON", "ART", "ARTHUR", "ARVEL", "ARYANA", "ASH", "ASHER", "ASHLEA", "ASHLEE", "ASHLEIGH", "ASHLEY", "ASHLIE", "ASHLYN", "ASHLYNN", "ASHTON", "ASIA", "ASPEN", "ASTON", "ASTOR", "ASTRA", "ATHENA", "AUBERON", "AUBREE", "AUBREY", "AUBRIE", "AUDIE", "AUDLEY", "AUDRA", "AUDREA", "AUDREY", "AUGUST", "AUGUSTA", "AUGUSTINE", "AURA", "AUREOLE", "AURORA", "AUSTEN", "AUSTIN", "AUSTYN", "AUTUMN", "AVA", "AVALINE", "AVALON", "AVELINE", "AVERILL", "AVERY", "AVICE", "AVIS", "AVRIL", "AYDAN", "AYDEN", "AYLMER", "AZALEA", "AZURA", "AZURE", "BABS", "BAILEE", "BAILEY", "BALDRIC"
		); 
		String[] s = new String[]{"1:0", "0:1", "0.5:0.5"};

		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 101; i++)
		{
			sb.append(list.get(i) + " - " + list.get(i + 100) + " - " + s[Math.abs(random.nextInt() % 3)]).append(System.lineSeparator());
		}
		System.out.println(sb.toString());
	}
}