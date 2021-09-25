package world;

import java.util.ArrayList;
import java.util.List;

import world.element.unmovable.Unmovable;
import world.movable.Movable;

public abstract class World {
	public List<Unmovable> unmovables = new ArrayList<>();
	public List<Movable> movables = new ArrayList<>();
	public Unmovable exit;
}
