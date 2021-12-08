package main

import 
(
	"errors"
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var 
(
	seedRand *rand.Rand = rand.New(rand.NewSource(time.Now().UnixNano()))
	charset             = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
)

type Result struct
 {
	index  int
	result string
}

func main() 
{
	initRand()
	totalTasks := 10
	var tasks []func() (string, error)
	for i := 0; i < totalTasks; i++ 
	{
		tasks = append(tasks, extract)
	}
	results := ConcurrentRetry(tasks, 20, 2)
	for r := range results 
	{
		fmt.Sprintf("The index of the result was: %v \n The result was: %v", r.index, r.result)
	}
}

func initRand()
{
	rand.Seed(seedRand.Int63n(seedRand.Int63()))
}

func randomString(length int) string 
{
	b := make([]byte, length)
	for i := range b 
	{
		b[i] = charset[rand.Intn(len(charset))]
	}
	return string(b)
}

func extract() (string, error) 
{
	length := rand.Intn(8)
	if length < 4 
	{
		return "", errors.New("length equals 0")
	}
	return randomString(length), nil
}

func ConcurrentRetry(tasks []func() (string, error), concurrent int, retry int) <-chan Result {
	threads := make(chan func() (string, int, error), len(tasks))
	results := make(chan Result, len(tasks))

	var waiting sync.WaitGroup

	for x := 1; x <= concurrent; x++ 
	{
		go worker(x, threads, results, len(tasks), &waiting)
	}

	for i := 0; i < len(tasks); i++ 
	{
		waiting.Add(1)
		exec := tasks[i]
		ind := i
		task := func() (string, int, error) 
		{
			str, err := exec()
			return str, ind, err
		}
		threads <- task
	}
	close(threads)

	go func() 
	{
		waiting.Wait()
		close(results)
	}()

	return results

}

func worker(ID int, jobs <-chan func() (string, int, error), result chan<- Result, retry int, wg *sync.WaitGroup)
 {
	fmt.Println("The worker with the ID:", ID, "Spawned and ready to work...")
	for task := range jobs 
	{
		failed := true
		for i := 0; i < retry; i++ 
		{
			str, ind, err := task()
			if err == nil 
			{
				fmt.Println("The worker with the ID:", ID, "finished task in", i+1, "attempts.")
				toSend := Result{ind, str}
				defer wg.Done()
				failed = false
				result <- toSend
				break
			}
			fmt.Println("The worker with the ID:", ID, " is now retrying the task from index", ind)
		}
		if failed 
		{
			fmt.Println("The worker with the ID:", ID, "failed to finish task in ", retry, "attempts.")
			defer wg.Done()
		}
	}
}
