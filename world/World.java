package world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import world.element.movable.Movable;
import world.element.unmovable.Unmovable;

public abstract class World implements Serializable {
	// won't be more child class logically
	public List<Unmovable> unmovables = new ArrayList<>();
	public List<Movable> movables = new ArrayList<>();

	public Unmovable exit;
}
