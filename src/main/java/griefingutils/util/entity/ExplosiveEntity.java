package griefingutils.util.entity;

public enum ExplosiveEntity {
    FIREBALL(EggGenerator.FIREBALL, true),
    TNT(EggGenerator.TNT, false),
    CREEPER(EggGenerator.CREEPER, true);

    public final EggGenerator generator;
    public final boolean hasCustomExplosionSize;

    ExplosiveEntity(EggGenerator generator, boolean hasCustomExplosionSize) {
        this.generator = generator;
        this.hasCustomExplosionSize = hasCustomExplosionSize;
    }
}
