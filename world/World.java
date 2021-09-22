package world;

import java.util.ArrayList;
import java.util.List;

import world.element.Movable;
import world.element.Unmovable;

public abstract class World {
	List<Unmovable> objectList = new ArrayList<>();
	List<Movable> characterList = new ArrayList<>();
	Unmovable exit;
}
