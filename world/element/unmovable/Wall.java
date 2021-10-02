package world.element.unmovable;

import world.element.Animation;

public class Wall extends Unmovable {
	public Wall() {
		super(new Animation(0, "resource/unmovable/wall"));
	}
}
