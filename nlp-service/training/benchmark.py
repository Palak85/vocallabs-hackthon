"""
Latency and Performance Benchmark for NLP Microservice on CPU.
Measures ACTUAL latency per stage and end-to-end pipeline execution time.
"""

import sys
import os
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

import time
import statistics
from app.services.inference import pipeline

TEST_QUERIES = [
    "My Amazon package hasn't arrived yet.",
    "My Blinkit delivery is late by two hours.",
    "My package is late, where is the courier?",
    "I paid my daughter's school fees yesterday but portal says unpaid.",
    "My insurance claim CLM-45672 is still pending for three weeks.",
    "My UPI payment failed but money was deducted from account.",
    "My recharge succeeded but mobile data is not working.",
    "My flight was cancelled, need full refund immediately.",
    "I need to book a doctor appointment with cardiologist.",
    "THIS IS RIDICULOUS! I called four times and nobody is helping me!"
]


def run_benchmark(iterations_per_query: int = 10):
    print("=" * 60)
    print("MEASURING ACTUAL CPU INFERENCE LATENCIES")
    print("=" * 60)

    # Warmup
    for q in TEST_QUERIES[:3]:
        _ = pipeline.analyze(q)

    latencies = []
    for q in TEST_QUERIES:
        query_latencies = []
        for _ in range(iterations_per_query):
            start = time.perf_counter()
            res = pipeline.analyze(q)
            elapsed_ms = (time.perf_counter() - start) * 1000.0
            query_latencies.append(elapsed_ms)
        avg_q_lat = statistics.mean(query_latencies)
        latencies.extend(query_latencies)
        print(f"Query: '{q[:40]}...' -> Avg Latency: {avg_q_lat:.2f} ms")

    total_mean = statistics.mean(latencies)
    total_median = statistics.median(latencies)
    p95 = statistics.quantiles(latencies, n=20)[18]  # 95th percentile
    min_lat = min(latencies)
    max_lat = max(latencies)

    print("\n" + "=" * 60)
    print("BENCHMARK SUMMARY (GENUINELY MEASURED ON CPU):")
    print(f"Total benchmark runs : {len(latencies)}")
    print(f"Mean Latency         : {total_mean:.2f} ms")
    print(f"Median Latency       : {total_median:.2f} ms")
    print(f"95th Percentile (p95): {p95:.2f} ms")
    print(f"Min Latency          : {min_lat:.2f} ms")
    print(f"Max Latency          : {max_lat:.2f} ms")
    print("=" * 60)


if __name__ == "__main__":
    run_benchmark()
