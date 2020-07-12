package scene.entity;

import dev.Console;
import geom.AABB;
import scene.Scene;
import scene.entity.utility.PhysicsEntity;
import util.RunLengthInputStream;
import util.RunLengthOutputStream;


public class PlayerEntity extends PhysicsEntity {

	public PlayerEntity(Scene scene) {
		super(null, null);
		position.set(scene.getCamera().getPosition());
		visible = false;
		PlayerHandler.setEntity(this);
		persistency = 3;
	}

	@Override
	public void update(Scene scene) {
		if (this.getChunk() == null) return;
		PlayerHandler.update(scene);
		super.update(scene);
	}
	
	@Override
	public void jump(float height) {
		super.jump(height);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	@Override
	public void save(RunLengthOutputStream data) {
	}

	@Override
	public void load(RunLengthInputStream data) {
	}

	public AABB getAABB() {
		return aabb;
	}
}