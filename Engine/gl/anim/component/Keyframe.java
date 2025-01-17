package gl.anim.component;

import java.util.Map;

import org.joml.Quaternion;
import org.joml.Vector3f;

public class Keyframe {

	private final float time;
	private final Map<Byte, JointTransform> transforms;

	public Keyframe(float time, Map<Byte, JointTransform> transforms) {
		this.time = time;
		this.transforms = transforms;
	}

	public float getTime() {
		return time;
	}

	public Map<Byte, JointTransform> getTransforms() {
		return transforms;
	}
}
