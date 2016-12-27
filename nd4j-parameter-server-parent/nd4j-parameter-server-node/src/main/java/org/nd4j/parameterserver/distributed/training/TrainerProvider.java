package org.nd4j.parameterserver.distributed.training;

import lombok.NonNull;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.parameterserver.distributed.conf.Configuration;
import org.nd4j.parameterserver.distributed.logic.Clipboard;
import org.nd4j.parameterserver.distributed.messages.TrainingMessage;
import org.nd4j.parameterserver.distributed.transport.Transport;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author raver119@gmail.com
 */
public class TrainerProvider {
    private static final TrainerProvider INSTANCE = new TrainerProvider();

    // we use Class.getSimpleName() as key here
    protected Map<String, TrainingDriver<?>> trainers = new HashMap<>();

    protected Configuration configuration;
    protected Transport transport;
    protected Clipboard clipboard;

    private TrainerProvider(){
        scanClasspath();
    }

    public static TrainerProvider getInstance() {
        return INSTANCE;
    }

    protected void scanClasspath() {
        // TODO: reflection stuff to fill trainers
        Reflections reflections = new Reflections("org");
        Set<Class<? extends TrainingDriver>> classes = reflections.getSubTypesOf(TrainingDriver.class);

        for (Class clazz: classes) {
            if (clazz.isInterface() ||  Modifier.isAbstract(clazz.getModifiers() ))
                continue;

            try {
                TrainingDriver driver = (TrainingDriver) clazz.newInstance();
                trainers.put(driver.targetMessageClass(), driver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (trainers.size() < 1)
            throw new ND4JIllegalStateException("No TrainingDrivers were found");
    }

    public void init(@NonNull Configuration configuration, @NonNull Transport transport, @NonNull Clipboard clipboard) {
        this.configuration = configuration;
        this.transport = transport;
        this.clipboard = clipboard;

        for (TrainingDriver<?> trainer: trainers.values()) {
            trainer.init(configuration, transport, clipboard);
        }
    }



    @SuppressWarnings("unchecked")
    protected  <T extends TrainingMessage> TrainingDriver<T> getTrainer(T message){
        TrainingDriver<?> driver = trainers.get(message.getClass().getSimpleName());
        if (driver == null)
            throw new ND4JIllegalStateException("Can't find trainer for [" + message.getClass().getSimpleName() + "]");

        return (TrainingDriver<T>) driver;
    }

    public <T extends TrainingMessage> void doTraining(T message) {
        TrainingDriver<T> trainer = getTrainer(message);
        trainer.doTraining(message);
    }
}