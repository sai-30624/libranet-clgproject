import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/axios";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { AuthLayout } from "./AuthLayout";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await api.post("/auth/login", { email, password });
      const { token, type, ...userData } = res.data;
      login(token, userData);
      navigate("/");
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Failed to login. Please check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <Card className="border-white/10 bg-black/40 backdrop-blur-xl shadow-2xl rounded-2xl overflow-hidden">
        <div className="absolute top-0 inset-x-0 h-px bg-gradient-to-r from-transparent via-violet-500/50 to-transparent"></div>
        <CardHeader className="space-y-1 pb-6 pt-8">
          <CardTitle className="text-2xl text-center text-white/90">Welcome back</CardTitle>
          <CardDescription className="text-center text-zinc-400">
            Enter your credentials to access your account
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleLogin} className="space-y-5">
            {error && (
              <div className="p-3 text-sm text-red-200 bg-red-950/50 border border-red-900/50 rounded-lg text-center backdrop-blur-sm">
                {error}
              </div>
            )}
            <div className="space-y-2.5">
              <Label htmlFor="email" className="text-zinc-300 font-medium">Email address</Label>
              <Input 
                id="email" 
                type="email" 
                placeholder="student@university.edu" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="bg-black/50 border-white/10 text-white placeholder:text-zinc-600 focus-visible:ring-violet-500 focus-visible:border-violet-500/50 h-11"
              />
            </div>
            <div className="space-y-2.5">
              <Label htmlFor="password" className="text-zinc-300 font-medium">Password</Label>
              <Input 
                id="password" 
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="bg-black/50 border-white/10 text-white focus-visible:ring-violet-500 focus-visible:border-violet-500/50 h-11"
              />
            </div>
            <Button 
              type="submit" 
              className="w-full bg-violet-600 hover:bg-violet-500 text-white transition-all shadow-[0_0_20px_-5px_rgba(124,58,237,0.4)] hover:shadow-[0_0_25px_-5px_rgba(124,58,237,0.6)] h-11 text-base font-medium rounded-lg mt-2"
              disabled={loading}
            >
              {loading ? "Signing in..." : "Sign in"}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex justify-center border-t border-white/5 pt-5 pb-6 bg-white/[0.02]">
          <p className="text-sm text-zinc-400">
            Don't have an account?{" "}
            <Link to="/register" className="text-violet-400 hover:text-violet-300 font-medium transition-colors">
              Sign up
            </Link>
          </p>
        </CardFooter>
      </Card>
    </AuthLayout>
  );
}
