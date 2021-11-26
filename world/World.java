package world;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import world.element.movable.Movable;
import world.element.unmovable.Unmovable;

public abstract class World implements Serializable {
	// won't be more child class logically => no need to use heterogeneous
	// collection
	public List<Unmovable> unmovables = new LinkedList<>();
	public List<Movable> movables = new LinkedList<>();

	public int height;
	public int width;

	public Unmovable exit;
}
