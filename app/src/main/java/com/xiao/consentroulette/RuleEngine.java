package com.xiao.consentroulette;

import java.time.LocalDate;
import java.util.*;

/** Deterministic, non-explicit implementation of the wheel's daily score mechanics. */
public final class RuleEngine {
  public static final int LOCKED = 10, DISABLED = -5;
  public enum Die { X, Y }
  public enum Choice { MINUS_TWO, HALF_UP, PLUS_FOUR, DOUBLE }
  public static final class Result {
    public final int before, after; public final boolean disabled, locked;
    Result(int before, int after) { this.before=before; this.after=after; disabled=after<=DISABLED; locked=after>=LOCKED; }
  }
  public static int roll(Random random) { return random.nextInt(6) + 1; }
  public static Result apply(int score, Die die, int value, Choice choice) {
    if (score <= DISABLED || score >= LOCKED) return new Result(score, score);
    if (value < 1 || value > 6) throw new IllegalArgumentException("骰子必须为 1 到 6");
    int delta;
    if (die == Die.X) {
      switch(value) { case 1: delta = choose(score, choice, -2, (int)Math.ceil(score/2.0)); break; case 2: case 3: delta=-1; break; case 4: delta=0; break; case 5: delta=1; break; default: delta=4; }
      return new Result(score, clamp(score + delta));
    }
    switch(value) { case 1: delta=choose(score, choice, -2, (int)Math.ceil(score/2.0)); return new Result(score,clamp(score+delta)); case 2: delta=0; break; case 3: delta=1; break; case 4: delta=2; break; case 5: delta=3; break; default: if(choice==Choice.DOUBLE) return new Result(score,clamp(score*2)); delta=4; }
    return new Result(score, clamp(score+delta));
  }
  private static int choose(int score, Choice c, int a, int b) { return c==Choice.HALF_UP ? b : a; }
  private static int clamp(int score) { return Math.max(DISABLED, Math.min(LOCKED, score)); }
  public static boolean canPlayToday(String lastPlayed, LocalDate today) { return lastPlayed == null || !lastPlayed.equals(today.toString()); }
}
