package world;

import java.util.ArrayList;
import java.util.List;

import world.element.Movable;
import world.element.Unmovable;

public abstract class World {
	public List<Unmovable> objectList = new ArrayList<>();
	public List<Movable> characterList = new ArrayList<>();
	public Unmovable exit;
}
