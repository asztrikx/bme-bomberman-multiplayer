package world;

import java.util.ArrayList;
import java.util.List;

import world.element.unmovable.Unmovable;
import world.movable.Movable;

public abstract class World {
	public List<Unmovable> objectList = new ArrayList<>();
	public List<Movable> characterList = new ArrayList<>();
	public Unmovable exit;
}
