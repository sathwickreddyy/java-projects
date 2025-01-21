package com.java.patterns;

/**
 * Why we need design patterns
 */
public class WhyWithSimUDuck {

    public static void main(String[] args) {
        System.out.println("With Inheritance:-\n");
        approachUsingNormalOOPsInheritance();
        // Now what happens if we want to add fly method i.e., some ducks can fly.
        // We cannot directly add fly method to parent class here & Also rubber ducks don't quack instead squeak
        /*
         * Issue with Inheritance here is:
         *
         * Code is duplicated across subclasses by unnecessary overrides
         * Runtime behaviors changes are difficult
         * It's hard to gain knowledge of all duck behaviors
         * Changes can unintentionally affect other ducks.
         */
        System.out.println("\nWith Separate Interfaces for Quack and Fly i.e., Behavior is very specific to subclass:-\n");
        approachUsingInterfaces();
        // Can I say here lots of duplicate code in subclasses?
        // For example if we want to modify the flying behavior for all the ducks -  we must update the entire logic of
        // all the subclasses here in the future. Code reuse is a nightmare in the future.
        // However, we cannot directly use a polymorphism here
    }

    public static void approachUsingNormalOOPsInheritance() {
        /*
         * Normal OOPs Way
         *
         * All ducks can swim, quack and can be shown on display
         */
        abstract class Duck
        {
            void quack(){
                System.out.println("Quack");
            }

            void swim() {
                System.out.println("Swimming");
            }

            abstract void display();
        }

        class MallardDuck extends Duck
        {
            @Override
            public void display() {
                System.out.println("Mallard Duck");
            }
        }

        class RedHeadDuck extends Duck
        {

            @Override
            void display() {
                System.out.println("Redhead Duck");
            }
        }
        Duck mallardDuck = new MallardDuck();
        Duck redHeadDuck = new RedHeadDuck();
        mallardDuck.display();
        redHeadDuck.display();
    }

    public static void approachUsingInterfaces() {
        abstract class Duck
        {
            void swim() {
                System.out.println("Swimming");
            }

            abstract void display();
        }

        interface Quackable {
            void quack();
        }

        interface Flyable {
            void fly();
        }

        class MallardDuck extends Duck implements Quackable, Flyable
        {
            @Override
            public void display() {
                System.out.println("Mallard Duck");
            }

            @Override
            public void quack() {
                System.out.println("Quack Quack");
            }

            @Override
            public void fly() {
                System.out.println("Mallard Fly");
            }
        }

        class RubberDuck extends Duck implements Quackable
        {
            @Override
            void display() {
                System.out.println("Rubber Duck");
            }

            @Override
            public void quack() {
                System.out.println("Squeak");
            }
        }

        class DecoyDuck extends Duck
        {

            @Override
            void display() {
                System.out.println("DecoyDuck");
            }
        }
        Duck mallardDuck = new MallardDuck();
        Duck rubberDuck = new RubberDuck();
        Duck decoyDuck = new DecoyDuck();
        mallardDuck.display();
//        mallardDuck.quack(); // Doesnt work with parent class compile time issue
        MallardDuck mallardDuck1 = new MallardDuck();
        mallardDuck1.quack();
        mallardDuck1.fly();
        RubberDuck rubberDuck1 = new RubberDuck();
        rubberDuck1.display();
        rubberDuck1.quack();
        decoyDuck.display();
    }

}