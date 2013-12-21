/*-
 * Copyright 2013 © Meikel Brandmeyer.
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package clojuresque.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.TaskAction

/**
 * Execute a set of tasks in a regular interval. This can be useful when
 * to generate eg. a new set of assets during web development. Or run
 * tests continually during development.
 *
 * <strong>Note:</strong> This is somewhat hacky. Expect problems.
 *
 * @author Meikel Brandmeyer &lt;mb@kotka.de&gt;
 */
class TaskWatcher extends DefaultTask {
    /**
     * The sleeping interval between task runs. The time is measured in
     * milli seconds. Default: 5s.
     */
    def pollingInterval = 5000

    /**
     * Get the tasks, watched by this watcher task.
     */
    def getTasks() {
        watcherTask().tasks
    }

    /**
     * Set the tasks, watched by this watcher task. The argument must be
     * suitable for the <code>tasks</code> property of
     * <code>GradleBuild</code>.
     *
     * @param  tasks The tasks to watch
     * @return       <code>this</code>
     */
    def setTasks(tasks) {
        watcherTask().tasks = tasks
        this
    }

    /**
     * Add a task to be watched by this watcher task. The argument must
     * be suitable for the <code>tasks</code> property of
     * <code>GradleBuild</code>.
     *
     * @param  t The task to watch
     * @return   <code>this</code>
     */
    def task(t) {
        watcherTask().tasks << t
        this
    }

    /**
     * The task action. Call only if you know what you are doing.
     */
    @TaskAction
    def void watch() {
        def t = watcherTask()

        if (t.tasks.size() == 0) {
            logger.info "No tasks to watch. Bailing out."
            return
        }

        logger.info "Watching the following tasks (with ${pollingInterval}ms interval):"
        t.tasks.each { logger.info " * ${it}" }

        while (true) {
            Thread.sleep(pollingInterval)
            logger.info "Polling..."
            t.actions.each { it.execute(t) }
        }
    }

    def private watcherTask() {
        try {
            project.tasks["watchedTaskExecutor"]
        } catch (UnknownTaskException exc) {
            project.task("watchedTaskExecutor", type: GradleBuild)
        }
    }
}
