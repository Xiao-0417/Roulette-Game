package com.xiao.consentroulette;
import static org.junit.Assert.*;
import org.junit.Test;
import java.time.LocalDate;
public class RuleEngineTest {
 @Test public void xAndYRulesMatchScoreTable() { assertEquals(-2, RuleEngine.apply(0, RuleEngine.Die.X,1,RuleEngine.Choice.MINUS_TWO).after); assertEquals(1, RuleEngine.apply(0, RuleEngine.Die.X,5,null).after); assertEquals(4, RuleEngine.apply(0,RuleEngine.Die.X,6,null).after); assertEquals(3, RuleEngine.apply(0,RuleEngine.Die.Y,5,null).after); assertEquals(8,RuleEngine.apply(4,RuleEngine.Die.Y,6,RuleEngine.Choice.DOUBLE).after); }
 @Test public void boundsDisableAndLockScores() { assertEquals(-5, RuleEngine.apply(-4,RuleEngine.Die.X,1,RuleEngine.Choice.MINUS_TWO).after); assertEquals(10,RuleEngine.apply(9,RuleEngine.Die.X,6,null).after); assertEquals(10,RuleEngine.apply(10,RuleEngine.Die.Y,1,RuleEngine.Choice.MINUS_TWO).after); }
 @Test public void dailyGateUsesCalendarDate() { LocalDate d=LocalDate.of(2026,10,2); assertFalse(RuleEngine.canPlayToday("2026-10-02",d)); assertTrue(RuleEngine.canPlayToday("2026-10-01",d)); }
}
