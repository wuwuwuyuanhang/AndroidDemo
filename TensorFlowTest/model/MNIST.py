# @Auther : wuwuwu 
# @Time : 2020/4/20 
# @File : MNIST.py
# @Description : 训练MNIST

import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import Sequential, layers, optimizers, losses

(x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()

x_train = x_train.reshape((-1, 28, 28, 1))
x_test = x_test.reshape((-1, 28, 28, 1))

x_train = x_train / 255.
x_test = x_test / 255.

model = Sequential([
    layers.Conv2D(16, (3, 3), activation='relu'),
    layers.MaxPool2D((2, 2)),
    layers.Conv2D(32, (3, 3), activation='relu'),
    layers.MaxPool2D((2, 2)),
    layers.Flatten(),
    layers.Dense(64, activation='relu'),
    layers.Dense(32, activation='relu'),
    layers.Dense(10, activation='softmax')
])

model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

model.build(input_shape=(None, 28, 28, 1))
model.summary()

history = model.fit(x_train, y_train, epochs=10)

test_loss = model.evaluate(x_test, y_test)
print(test_loss)

tf.saved_model.save(model, "saved/1")