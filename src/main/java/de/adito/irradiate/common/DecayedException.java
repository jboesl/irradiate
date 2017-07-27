package de.adito.irradiate.common;

/**
 * @author j.boesl, 27.07.17
 */
public class DecayedException extends RuntimeException
{
  public DecayedException()
  {
    super("Particle is decayed.");
  }
}
