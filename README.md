# Stacks ECS
`Stacks` is a lightweight ECS (Entity-Component-System) library for Java which focuses on functional paradigms and minimal heap allocation.

## Frequently Asked Questions

### What is Stacks?
See the description above. The name "`Stacks`" comes from an emphasis on stack-based memory management over heap-based memory management.  Beyond that, Stacks is a pet project for an individual developer to work on "outside the box" development skills.

### Why was Stacks made?
Stacks emerged out of a game development project for a sole developer who wanted to push the boundaries of convential Java memory management while also developing a stronger understanding of functional programming.  It is not intended to be a conventionally good or useful ECS library.

### Why should I use Stacks?
You should not use Stacks. It is a pet project and really should not be considered for conventional use.  However, if you are weird like the original developer of Stacks then using and/or reading the source code for Stacks may be educational for you.

### If no one else should be using Stacks, why does this README exist?
Simply for practice with writing READMEs, and as a caution against using this project just because it appears on GitHub in a public repo.

## The Stacks Instance
The class `stacks.Stacks` is a sort of "god object" through which all of the features of the library can be exposed.  We say "sort of" for two reasons: because nothing in `Stacks` is *really* an object (the framework uses functional interfaces almost exclusively), and the `Stacks` object is really just a shallow facade over several other "objects" with their own distinct roles (and those in turn are shallow facades over other more specialized "objects").

### Dollar ('$') Methods
For brevity, most operations in `Stacks` can be performed using the heavily overloaded `$()` method.
- `$()` will start a new `ManagedTransaction`. [See "Transactions" below.](#transactions)
- `$(Component c, Component... cs)` or `$(Component[] cs)` will return a `Stream<ManagedEntity>` matching all provided components. [See "Entities" below.](#entities)
- `$(Long e, Long... es)` or `$(Long[] es)` will return a `Stream<ManagedEntity>` containing all entities with matching IDs.
- `$(Class<T> c, String n)` will return an `Optional<T>` from the `Library` with the matching type `c` and name `n`.  [See "The Library" below.](#the-library)

### Initializing Stacks
There are two ways to initialize `Stacks`:
- Use `Stacks.create()` or `Stacks.create(Properties p)` to produce an instance of `Stacks`.  This is the option to use if for some reason you need multiple instances of `Stacks` or you want to have your DI/IoC framework inject an instance.
- Use the static `$` methods of `StacksGlobal` (such as with `import static`).  Calling any such method will initialize an internal singleton instance of `Stacks`, memoized for ongoing use.

## Transactions
`Stacks` does not permit direct editing of entities.  Instead, all entity operations are committed as part of a `Transaction`.  A `Transaction` is simply a record of change to be made against one or more entity writers.  `Transaction`s can also be chained together in composition, meaning any single `Transaction` could actually represent an entire sequence of changes.  The general flow of a `Processor` (the `Stacks` name for an ECS "System") will produce `Transaction` chains for the `Entity`s they manage.  `Stacks` handles committing the `Transaction` chain against the writer.  [See "Processors" for more on this.](#processors)

## Components
`Stacks` uses a minimalist approach to `Component`s wherein a single component generally represents a single data element of an entity.  There are five different data types to describe a `Component`'s data element:
- NONE - This `Component` has no additional data, meaning it is used as a "flag", to simply indicate whether or not the `Entity` has a particular characteristic.
- STRING - It is associated with a textual value.
- NUMBER - It is associated with a numeric value.  Note that this uses the Java Long type and there is no `Component` data type for decimal/floating-point numbers.  For storing decimal values consider implementing an "implied decimal point" (ie. a Long where the ones and tens digits are actually the hundredths and tenths digits).
- REFERENCE - It is associated with another `Entity`.  These sorts of `Component`s can be used to define a relationship between two `Entity`s.
- MULTIREF - It is associated with multiple other `Entity`s.  This is the one exception to `Component`s storing only a single data element.  This is present to the allow for the implementation of things which require a one-to-many relationship, such as containers.

### Special Components
`Stacks` provides three built-in `Component`s which are required in order for the system to function correctly:
- IDENTITY - A REFERENCE which refers to the `Entity` itself.  This is actually how the `Entity` even knows its own ID value, so every `Entity` must have this `Component` and it should never be removed or changed after `Entity` creation.
- TRANSIENT - A flag which indicates whether or not the `Entity` is suitable for long-term/persistent storage.  Each game will have its own requirements for this, and for games which only write to one location this may be irrelevant.  However, the JDBC/Database storage provided by `Stacks` will ignore any `Entity` that is created as TRANSIENT.  For this reason, there are separate `Entity` creation methods for Transient `Entity`s.
- EXPIRED - A flag which indicates whether or not the `Entity` still needs to be retained.  Like TRANSIENT, different storage systems can handle this differently.  The memory storage provided by `Stacks` will delete 'Entity's marked as EXPIRED, and the delivered JDBC/Database storage will exclude them from results.

## Entities
Just like everything else in `Stacks`, entities aren't objects: they are an abstract wrapper around an identifier and a bag of components.  Creating an entity is fairly straightforward:
```java
$().createEntity().commit();
```
This example isn't good functional form, but it does the trick: you will end up with a new entity with a new ID and no components other than `IDENTITY`.  Generally speaking, you won't do this.  The following examples are more useful.
```java
// Create an entity with one or more "flag" components
$().createEntity("NEW", "MOB", "ANGRY").commit();

// Create an entity with a seeded ID to use for reference
final Long id = StacksGlobal.nextId();
$().createEntity(id).commit();

// Create an entity using a template and parameters
final EntityTemplate soldierTemplate = EntityTemplate.fromProperties("soldier");
$().createEntitiesFromTemplate(soldierTemplate, "Steve the Soldier", 23).commit();
// ...or...
$().createEntitiesFromTemplate("soldier", "Steve the Soldier", 23).commit();
```
By default, when an entity is committed it is created in both memory and persistent storage.  If you want to create an entity in memory only you should use the `createTransientEntity()` methods instead.  [See "Special Components" above.](#special-components)

## The Library
The purpose of the `Library` is to serve as a repository of static information for `Stacks`.  Generally speaking there is usually only one `Library` per `Stacks` instance.  The `Library` is comprised of several `Dictionary` (with the `Library` itself being a `Dictionary` of `Dictionary`).  Each `Dictionary` in the `Library` functions like a `Map` but is associated with a `Class` type, and the contents of the `Dictionary` are instances of that type referenced by some lookup `String`.  This makes the `Library` very good for storing typally organized information, such as `Configuration`, or even `Component` definitions.  For example...
```java
// Look up a Component by name
Optional<Component> foo = (Component.class, "FOO");
```

### The Librarian
The `Librarian` is a special interface for managing the contents of a `Library`.  For example...
```java
// Register a new Component
Component foo = Component.of("FOO", Component.DataType.STRING);
StacksGlobal.stacks().librarian().registerEntry(Component.class, foo.name() foo);
```

## Processors
A `Processor` fills the role of an ECS System.  We use the name "`Processor`" to more accurately describe the role these components fulfill and also to differentiate it from the core Java `System` class.

A `Processor` is a defined module of game logic that will perform operations on `Entity`s having a given `Component`.  It accepts an `Entity` as input and produces a `Transaction` (which could be a chained `Transaction`) as output.  In this way, it implements the behaviors of objects in the game world. 

A `Processor` can also be built up from multiple `Subprocessor`s that operate in sequence, with each `Subprocessor` potentially specifying a different component than the parent and causing that `Subprocessor` to only operate on `Entity`s where its `Component` and its parent's `Component` intersect.

## Internal Structures
`Stacks` implements a number of custom data structures in order to adhere somewhat to functional paradigms.  You do not need to utilize these structures in your code as all the important `Stacks` methods only return commonly accepted Java structures like `Stream`s and `Optional`s.  However, you could use them in your own code if desired.

### Tuple, Single, Couple, and Triple
A `Tuple` is an ordered set of values similar to a `List`.  Tuples are used all over the backend of `Stacks` as a way to pass multiple values between function calls.  Even though they are generically typed, you can also "morph" a `Tuple`, which projects a view of a `Tuple` only containing the elements of the `Tuple` that can be safely cast to a new type.  `Single`, `Couple`, and `Triple` are special instances of `Tuple` designed to carry exactly one, two, or three elements respectively.