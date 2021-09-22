import java.util.ArrayList;
import java.util.List;

public abstract class World {
	List<Thing> objectList = new ArrayList<>();
	List<Character> characterList = new ArrayList<>();
	Thing exit;
}
